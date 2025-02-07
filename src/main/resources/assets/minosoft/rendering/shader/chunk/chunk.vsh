/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

#version 330 core

layout (location = 0) in vec3 vinPosition;
layout (location = 1) in float vinAmbientUV;
layout (location = 2) in float vinIndexLayerAnimation; // texture index (0xF0000000), texture layer (0x0FFFF000), animation index (0x00000FFF)
layout (location = 3) in float vinLightTint; // Light (0xFF000000); 3 bytes color (0x00FFFFFF)

out vec3 finFragmentPosition;

uniform mat4 uViewProjectionMatrix;

#include "minosoft:vsh"
#include "minosoft:tint"
#include "minosoft:color"
#include "minosoft:light"
#include "minosoft:animation"

// #define DEBUG

#ifdef DEBUG
const vec3 AMBIENT_OCCLUSION[4] = vec3[4](
vec3(1.0f),
vec3(2.0f, 0.0f, 0.0f),
vec3(0.0f, 2.0f, 0.0f),
vec3(0.0f, 0.0f, 2.0f)
);
#else
const vec3 AMBIENT_OCCLUSION[4] = vec3[4](
vec3(1.0f),
vec3(0.85f),
vec3(0.75f),
vec3(0.60f)
);
#endif



void main() {
    gl_Position = uViewProjectionMatrix * vec4(vinPosition, 1.0f);
    uint lightTint = floatBitsToUint(vinLightTint);
    finTintColor = getRGBColor(lightTint & 0xFFFFFFu) * getLight(lightTint >> 24u);
    finFragmentPosition = vinPosition;


    uint ambientUV = floatBitsToUint(vinAmbientUV);
    finTintColor.rgb *= AMBIENT_OCCLUSION[(ambientUV >> 24u) & 0x3u];

    vec2 uv = uv_unpack(ambientUV);

    setTexture(uv, vinIndexLayerAnimation);
}
