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
package de.bixilon.minosoft.protocol.protocol

import de.bixilon.kutil.collections.map.bi.MutableBiMap
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.AbstractLatch.Companion.child
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.string.StringUtil.getBetween
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.AbstractServer
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.LANServer
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.eros.main.play.server.type.types.LANServerType
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.net.*
import java.nio.charset.StandardCharsets

object LANServerListener {
    const val BROADCAST_PORT = 4445
    const val BROADCAST_ADDRESS = "224.0.2.60"
    const val MAXIMUM_SERVERS = 100 // maximum number of lan servers, set because otherwise dos attacks would be easy

    val SERVERS: MutableBiMap<InetAddress, AbstractServer> = MutableBiMap()
    private const val MOTD_START_STRING = "[MOTD]"
    private const val MOTD_END_STRING = "[/MOTD]"
    private const val PORT_START_STRING = "[AD]"
    private const val PORT_END_STRING = "[/AD]"
    private val BROADCAST_MUST_CONTAIN = arrayOf(MOTD_START_STRING, MOTD_END_STRING, PORT_START_STRING, PORT_END_STRING)
    private var listeningThread: Thread? = null
    private var stop = false

    val listening: Boolean
        get() = listeningThread != null

    fun listen(latch: AbstractLatch?) {
        OtherProfileManager.selected::listenLAN.observe(this) {
            if (it && listeningThread == null) {
                startListener()
            } else if (!it && listeningThread != null) {
                stopListening()
            }
        }
        if (OtherProfileManager.selected.listenLAN) {
            val innerLatch = latch?.child(1)
            startListener(innerLatch)
            innerLatch?.await()
        }
    }

    fun stopListening() {
        val listeningThread = listeningThread ?: throw IllegalStateException("Not running!")
        stop = true
        listeningThread.interrupt()
    }

    private fun startListener(latch: AbstractLatch? = null) {
        stop = false
        val thread = Thread({
            try {
                val socket = MulticastSocket(BROADCAST_PORT)
                val inetAddress = InetAddress.getByName(BROADCAST_ADDRESS)
                socket.joinGroup(InetSocketAddress(inetAddress, BROADCAST_PORT), NetworkInterface.getByInetAddress(inetAddress))
                val buffer = ByteArray(256) // this should be enough, if the packet is longer, it is probably invalid
                Log.log(LogMessageType.NETWORK, LogLevels.VERBOSE) { "Listening for LAN servers..." }
                latch?.dec()
                while (true) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)
                        val broadcast = String(buffer, 0, packet.length, StandardCharsets.UTF_8)
                        Log.log(LogMessageType.NETWORK_IN, LogLevels.VERBOSE) { "LAN servers broadcast (${packet.address.hostAddress}:${packet.port}): $broadcast" }
                        val sender = packet.address
                        if (SERVERS.containsKey(sender)) {
                            // This guy sent us already a server, maybe just the regular 1.5 second interval, a duplicate or a DOS attack...We don't care
                            continue
                        }
                        val server = getServerByBroadcast(sender, broadcast)
                        if (SERVERS.containsValue(server)) {
                            continue
                        }
                        if (SERVERS.size > MAXIMUM_SERVERS) {
                            continue
                        }
                        Log.log(LogMessageType.NETWORK, LogLevels.INFO) { "Discovered LAN servers: ${server.address}" }
                        SERVERS[sender] = server
                        LANServerType.servers += server
                    } catch (ignored: Throwable) {
                    }
                    if (stop) {
                        break
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                latch?.dec()
            } finally {
                this.listeningThread = null
                SERVERS.clear()
                Log.log(LogMessageType.NETWORK, LogLevels.VERBOSE) { "Stop listening for LAN servers..." }
            }
        }, "LAN Server Listener")
        thread.start()
        this.listeningThread = thread
    }

    private fun getServerByBroadcast(address: InetAddress, broadcast: String): AbstractServer {
        // example: [MOTD]Bixilon - New World[/MOTD][AD]41127[/AD]
        for (mustContain in BROADCAST_MUST_CONTAIN) {
            require(broadcast.contains(mustContain)) { "Broadcast is invalid!" }
        }
        var rawAddress = broadcast.getBetween(PORT_START_STRING, PORT_END_STRING)
        if (rawAddress.contains(":")) {
            // weird, just extract the port
            rawAddress = rawAddress.split(":").toTypedArray()[1]
        }
        val port = rawAddress.toInt()
        check(port in 1024 until 65535) { "Invalid port: $port" }
        val motd = broadcast.getBetween(MOTD_START_STRING, MOTD_END_STRING)
        return LANServer(address = address.hostAddress + ":" + rawAddress, name = BaseComponent("LAN: #${SERVERS.size}: ", ChatComponent.of(motd)))
    }


    fun clear() {
        SERVERS.clear()
        LANServerType.servers.clear()
    }
}
