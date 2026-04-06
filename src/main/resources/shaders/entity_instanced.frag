    // -----------------------------------------------------------------
    // | This file is a part of Zdraw.                                 |
    // | Copyright (C) 2026 Sona Softworks, LLC. All rights reserved.  |
    // -----------------------------------------------------------------

#version 450 core

// ----------------------------------------------------------------- vtx in

in vec3 vAtlasCoord;
in vec2 vLightCoord;
in vec4 vColor;
in vec3 vNormal;
in vec4 vOverlayColor;

// ----------------------------------------------------------------- uniforms

layout(binding = 0) uniform sampler2DArray uMegaAtlas;
layout(binding = 1) uniform sampler2D      uLightmap;

uniform vec3 uLightDir0;
uniform vec3 uLightDir1;

// ----------------------------------------------------------------- frag out

out vec4 fragColor;

// ----------------------------------------------------------------- exec

void main() {
    // sample our fat atlas
    vec4 texColor = texture(uMegaAtlas, vAtlasCoord);
    if (texColor.a < 0.004) discard;

    // compute lighting
    vec3 N = normalize(vNormal);
    float lighting = clamp(
        0.4 + 0.6 * max(dot(N, normalize(uLightDir0)), 0.0)
            + 0.6 * max(dot(N, normalize(uLightDir1)), 0.0),
        0.0, 1.0
    );

    vec3 lightmap = texture(uLightmap, vLightCoord).rgb;

    // apply said lighting
    vec3 shadedRGB = clamp(texColor.rgb * vColor.rgb * lightmap * lighting, 0.0, 1.0);
    float shadedA  = texColor.a * vColor.a;

    vec3 finalRGB = vOverlayColor.a > 0.0
        ? mix(shadedRGB, vOverlayColor.rgb, vOverlayColor.a)
        : shadedRGB;

    // emit the fragment
    fragColor = vec4(finalRGB, shadedA);
}
