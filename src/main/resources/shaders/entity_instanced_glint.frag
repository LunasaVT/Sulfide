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

// ----------------------------------------------------------------- frag out

out vec4 fragColor;

// ----------------------------------------------------------------- exec

void main() {
    // sample the glint texture from our fat atlas
    vec4 glintSample = texture(uMegaAtlas, vAtlasCoord);

    // alpha test to discard fragments that are too transparent
    if (glintSample.a < 0.004) discard;

    // emit the fragment
    fragColor = vec4(
        glintSample.rgb * vColor.rgb * 0.5,
        glintSample.a   * 0.35
    );
}
