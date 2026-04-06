package graphics.sulfide.render.entity

import graphics.sulfide.engine.buffer.GpuBuffer
import graphics.sulfide.engine.pipeline.VertexAttribute
import graphics.sulfide.engine.pipeline.VertexFormat
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL45C
import org.lwjgl.system.MemoryUtil

object EntityCube {
    private val VERTICES = floatArrayOf(
        -0.5f, -0.5f, 0.5f, 0f, 0f,
        0.5f, -0.5f, 0.5f, 1f, 0f,
        0.5f, 0.5f, 0.5f, 1f, 1f,
        -0.5f, 0.5f, 0.5f, 0f, 1f,

        0.5f, -0.5f, -0.5f, 0f, 0f,
        -0.5f, -0.5f, -0.5f, 1f, 0f,
        -0.5f, 0.5f, -0.5f, 1f, 1f,
        0.5f, 0.5f, -0.5f, 0f, 1f,

        -0.5f, -0.5f, -0.5f, 0f, 0f,
        -0.5f, -0.5f, 0.5f, 1f, 0f,
        -0.5f, 0.5f, 0.5f, 1f, 1f,
        -0.5f, 0.5f, -0.5f, 0f, 1f,

        0.5f, -0.5f, 0.5f, 0f, 0f,
        0.5f, -0.5f, -0.5f, 1f, 0f,
        0.5f, 0.5f, -0.5f, 1f, 1f,
        0.5f, 0.5f, 0.5f, 0f, 1f,

        -0.5f, 0.5f, 0.5f, 0f, 0f,
        0.5f, 0.5f, 0.5f, 1f, 0f,
        0.5f, 0.5f, -0.5f, 1f, 1f,
        -0.5f, 0.5f, -0.5f, 0f, 1f,

        -0.5f, -0.5f, -0.5f, 0f, 0f,
        0.5f, -0.5f, -0.5f, 1f, 0f,
        0.5f, -0.5f, 0.5f, 1f, 1f,
        -0.5f, -0.5f, 0.5f, 0f, 1f
    )

    private val INDICES = shortArrayOf(
        0, 1, 2, 2, 3, 0,
        4, 5, 6, 6, 7, 4,
        8, 9, 10, 10, 11, 8,
        12, 13, 14, 14, 15, 12,
        16, 17, 18, 18, 19, 16,
        20, 21, 22, 22, 23, 20
    )

    const val INDEX_COUNT: Int = 36

    const val VERTEX_STRIDE: Int = 20

    val VERTEX_FORMAT = VertexFormat(
        stride = VERTEX_STRIDE,
        attributes = listOf(
            VertexAttribute(index = 0, componentCount = 3, type = GL11C.GL_FLOAT, relativeOffset = 0),   // position
            VertexAttribute(index = 1, componentCount = 2, type = GL11C.GL_FLOAT, relativeOffset = 12)   // uv
        )
    )

    lateinit var vertexBuffer: GpuBuffer
        private set

    lateinit var indexBuffer: GpuBuffer
        private set

    var isUploaded: Boolean = false
        private set

    fun upload() {
        check(!isUploaded) { "EntityCube.upload() called twice" }

        val vertexBytes = VERTICES.size.toLong() * Float.SIZE_BYTES
        val indexBytes = INDICES.size.toLong() * Short.SIZE_BYTES

        vertexBuffer = GpuBuffer(vertexBytes, GpuBuffer.FLAG_DYNAMIC)
        val vbuf = MemoryUtil.memAlloc(vertexBytes.toInt())
        try {
            vbuf.asFloatBuffer().put(VERTICES).flip()
            GL45C.nglNamedBufferSubData(vertexBuffer.handle, 0, vertexBytes, MemoryUtil.memAddress(vbuf))
        } finally {
            MemoryUtil.memFree(vbuf)
        }

        indexBuffer = GpuBuffer(indexBytes, GpuBuffer.FLAG_DYNAMIC)
        val ibuf = MemoryUtil.memAlloc(indexBytes.toInt())
        try {
            ibuf.asShortBuffer().put(INDICES).flip()
            GL45C.nglNamedBufferSubData(indexBuffer.handle, 0, indexBytes, MemoryUtil.memAddress(ibuf))
        } finally {
            MemoryUtil.memFree(ibuf)
        }

        isUploaded = true
    }

    fun destroy() {
        if (isUploaded) {
            vertexBuffer.close()
            indexBuffer.close()
            isUploaded = false
        }
    }
}

