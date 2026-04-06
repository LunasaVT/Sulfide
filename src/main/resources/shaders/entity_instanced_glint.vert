// -----------------------------------------------------------------
// | This file is a part of Zdraw.                                 |
// | Copyright (C) 2026 Sona Softworks, LLC. All rights reserved.  |
// -----------------------------------------------------------------

#version 450 core

// ----------------------------------------------------------------- constants

const vec3 FACE_NORMALS[6] = vec3[6](
    vec3( 0.0,  0.0,  1.0),
    vec3( 0.0,  0.0, -1.0),
    vec3(-1.0,  0.0,  0.0),
    vec3( 1.0,  0.0,  0.0),
    vec3( 0.0,  1.0,  0.0),
    vec3( 0.0, -1.0,  0.0)
);

// ----------------------------------------------------------------- vtx in

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aUV;

// ----------------------------------------------------------------- instance data

struct InstanceData {
    mat4  model;
    vec2  uvMatRow0;
    vec2  uvMatRow1;
    vec2  uvMatTrans;
    int   overlayColor;
    int   pad;
    int   packedUVs[12];
    int   lightU;
    int   lightV;
    int   packedColor;
    int   atlasLayer;
    float atlasU0;
    float atlasV0;
    float atlasUScale;
    float atlasVScale;
};

layout(std430, binding = 0) readonly buffer InstanceBuffer {
    InstanceData instances[];
};

// ----------------------------------------------------------------- uniforms

uniform mat4 uViewProjection;
uniform int  uInstanceOffset;

// ----------------------------------------------------------------- vtx out

out vec3 vAtlasCoord;
out vec2 vLightCoord;
out vec4 vColor;
out vec3 vNormal;
out vec4 vOverlayColor;

// ----------------------------------------------------------------- utils

vec2 unpackUV(int bits) {
    return vec2(float(bits & 0xFFFF), float((bits >> 16) & 0xFFFF)) / 65535.0;
}

// ----------------------------------------------------------------- exec

void main() {
    InstanceData inst = instances[gl_InstanceID + uInstanceOffset];

    gl_Position = uViewProjection * (inst.model * vec4(aPosition, 1.0));

    int faceIdx = gl_VertexID / 4;
    int uvBase  = faceIdx * 2;

    vec2 faceMin = unpackUV(inst.packedUVs[uvBase + 0]);
    vec2 faceMax = unpackUV(inst.packedUVs[uvBase + 1]);

    vec2 entityUV = mix(faceMin, faceMax, aUV);

    vec2 uvResult = vec2(
        dot(inst.uvMatRow0, entityUV) + inst.uvMatTrans.x,
        dot(inst.uvMatRow1, entityUV) + inst.uvMatTrans.y
    );

    vec2 tiledUV = fract(uvResult);

    vAtlasCoord = vec3(
        inst.atlasU0 + tiledUV.x * inst.atlasUScale,
        inst.atlasV0 + tiledUV.y * inst.atlasVScale,
        float(inst.atlasLayer)
    );

    vLightCoord = vec2(
        (float(inst.lightU) * 16.0 + 8.0) / 256.0,
        (float(inst.lightV) * 16.0 + 8.0) / 256.0
    );

    vColor = vec4(
        float( inst.packedColor        & 0xFF) / 255.0,
        float((inst.packedColor >>  8) & 0xFF) / 255.0,
        float((inst.packedColor >> 16) & 0xFF) / 255.0,
        float((inst.packedColor >> 24) & 0xFF) / 255.0
    );

    vOverlayColor = vec4(
        float( inst.overlayColor        & 0xFF) / 255.0,
        float((inst.overlayColor >>  8) & 0xFF) / 255.0,
        float((inst.overlayColor >> 16) & 0xFF) / 255.0,
        float((inst.overlayColor >> 24) & 0xFF) / 255.0
    );

    vNormal = normalize(mat3(inst.model) * FACE_NORMALS[faceIdx]);
}

