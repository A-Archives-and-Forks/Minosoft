/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.scoreboard

import de.bixilon.kmath.vec.vec2.f.MVec2f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective
import de.bixilon.minosoft.data.scoreboard.ScoreboardPositions
import de.bixilon.minosoft.data.scoreboard.ScoreboardScore
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.LayoutedGUIElement
import de.bixilon.minosoft.gui.rendering.gui.hud.elements.HUDBuilder
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.renderer.drawable.AsyncDrawable
import de.bixilon.minosoft.modding.event.events.scoreboard.*
import de.bixilon.minosoft.modding.event.events.scoreboard.team.TeamUpdateEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.util.Initializable
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ScoreboardSideElement(guiRenderer: GUIRenderer) : Element(guiRenderer), LayoutedElement, Initializable, AsyncDrawable {
    private val backgroundElement = ColorElement(guiRenderer, size = Vec2f.EMPTY, color = RenderConstants.TEXT_BACKGROUND_COLOR)
    private val nameBackgroundElement = ColorElement(guiRenderer, size = Vec2f.EMPTY, color = RenderConstants.TEXT_BACKGROUND_COLOR)
    private val nameElement = TextElement(guiRenderer, "", background = null, parent = this)
    private val scores: LockMap<String, ScoreboardScoreElement> = lockMapOf()

    override val layoutOffset: Vec2f
        get() = super.size.let { return@let Vec2f(guiRenderer.scaledSize.x - it.x, (guiRenderer.scaledSize.y - it.y) / 2) }
    override val skipDraw: Boolean
        get() = objective == null

    var objective: ScoreboardObjective? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            scores.clear()
            forceSilentApply()
        }

    init {
        _prefMaxSize = Vec2f(MAX_SCOREBOARD_WIDTH, -1f)
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2f, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        backgroundElement.render(offset, consumer, options)
        nameBackgroundElement.render(offset, consumer, options)

        nameElement.render(offset + Vec2f(HorizontalAlignments.CENTER.getOffset(size.x, nameElement.size.x), 0.0f), consumer, options)
        val offset = MVec2f(offset)
        offset.y += TEXT_PROPERTIES.lineHeight

        this.scores.lock.acquire()
        val scores = this.scores.unsafe.entries.sortedWith { a, b -> a.value.score.compareTo(b.value.score) }
        this.scores.lock.release()

        var index = 0
        for ((_, score) in scores) {
            score.render(offset.unsafe, consumer, options)
            offset.y += TEXT_PROPERTIES.lineHeight

            if (++index >= MAX_SCORES) {
                break
            }
        }
    }

    override fun forceSilentApply() {
        val objective = objective
        if (objective == null) {
            _size = Vec2f.EMPTY
            return
        }

        this.scores.lock.lock()
        this.scores.unsafe.clear()
        objective.scores.lock.acquire()
        for ((entity, score) in objective.scores) {
            this.scores.unsafe.getOrPut(entity) { ScoreboardScoreElement(guiRenderer, entity, score, this) }
        }
        objective.scores.lock.release()
        this.scores.lock.unlock()

        updateName()

        queueSizeRecalculation()
    }

    fun recalculateSize() {
        val objective = objective
        if (objective == null) {
            _size = Vec2f.EMPTY
            return
        }
        val size = MVec2f(MIN_WIDTH, TEXT_PROPERTIES.lineHeight)
        size.x = maxOf(size.x, nameElement.size.x)

        val scores = scores.toSynchronizedMap()


        for ((_, element) in scores) {
            element.forceSilentApply()
            size.x = maxOf(size.x, element.prefSize.x)
        }

        size.y += TEXT_PROPERTIES.lineHeight * minOf(MAX_SCORES, scores.size)



        _size = size.unsafe
        nameBackgroundElement.size = Vec2f(size.x, TEXT_PROPERTIES.lineHeight)
        backgroundElement.size = size.unsafe


        for ((_, element) in scores) {
            element.applySize()
        }
    }

    private fun queueSizeRecalculation() {
        cacheUpToDate = false
    }

    fun removeScore(entity: String) {
        scores.remove(entity) ?: return
        queueSizeRecalculation()
    }

    fun updateScore(entity: String, score: ScoreboardScore) {
        scores.synchronizedGetOrPut(entity) { ScoreboardScoreElement(guiRenderer, entity, score, this) }
        queueSizeRecalculation()
    }

    fun updateName() {
        nameElement.text = objective?.displayName ?: return
        queueSizeRecalculation()
    }

    override fun postInit() {
        val session = context.session
        session.events.listen<ObjectivePositionSetEvent> {
            if (it.position != ScoreboardPositions.SIDEBAR) {
                return@listen
            }

            this.objective = it.objective
        }
        session.events.listen<ScoreboardObjectiveUpdateEvent> {
            if (it.objective != this.objective) {
                return@listen
            }
            this.updateName()
        }
        session.events.listen<ScoreboardScoreRemoveEvent> {
            if (it.objective != this.objective) {
                return@listen
            }
            this.removeScore(it.entity)
        }
        session.events.listen<ScoreboardScorePutEvent> {
            if (it.objective != this.objective) {
                return@listen
            }
            this.updateScore(it.entity, it.score)
        }
        session.events.listen<ScoreTeamChangeEvent> {
            if (it.objective != this.objective) {
                return@listen
            }
            this.updateScore(it.entity, it.score)
        }
        session.events.listen<TeamUpdateEvent> {
            val objective = this.objective ?: return@listen
            for ((entity, score) in objective.scores) {
                if (it.team != score.team) {
                    continue
                }
                this.updateScore(entity, score)
            }
        }
    }

    override fun drawAsync() {
        // check if content was changed, and we need to re-prepare before drawing
        if (!cacheUpToDate) {
            recalculateSize()
        }
    }

    companion object : HUDBuilder<LayoutedGUIElement<ScoreboardSideElement>> {
        override val identifier: ResourceLocation = "minosoft:scoreboard".toResourceLocation()
        val TEXT_PROPERTIES = TextRenderProperties()
        const val MAX_SCORES = 15
        const val MIN_WIDTH = 30.0f
        const val MAX_SCOREBOARD_WIDTH = 200.0f

        override fun build(guiRenderer: GUIRenderer): LayoutedGUIElement<ScoreboardSideElement> {
            return LayoutedGUIElement(ScoreboardSideElement(guiRenderer))
        }
    }
}
