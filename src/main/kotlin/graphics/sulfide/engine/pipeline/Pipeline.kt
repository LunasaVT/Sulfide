package graphics.sulfide.engine.pipeline

import org.lwjgl.opengl.GL45C

class Pipeline(
    val shader: ShaderProgram,
    val state: PipelineState,
    val vertexFormat: VertexFormat? = null
) : AutoCloseable {
    val vao: Int = GL45C.glCreateVertexArrays()

    init {
        vertexFormat?.let { fmt ->
            for (attr in fmt.attributes) {
                GL45C.glEnableVertexArrayAttrib(vao, attr.index)
                GL45C.glVertexArrayAttribFormat(
                    vao,
                    attr.index,
                    attr.componentCount,
                    attr.type,
                    attr.normalized,
                    attr.relativeOffset
                )
                GL45C.glVertexArrayAttribBinding(vao, attr.index, 0)
            }
        }
    }

    private val uniformCache = HashMap<String, Int>(16)

    fun uniform(name: String): Int =
        uniformCache.getOrPut(name) { shader.uniformLocation(name) }

    fun bind() {
        shader.use()
        GL45C.glBindVertexArray(vao)
        state.apply()
    }

    override fun close() {
        GL45C.glDeleteVertexArrays(vao)
        shader.close()
    }
}