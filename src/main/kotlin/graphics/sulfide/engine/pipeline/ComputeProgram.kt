package graphics.sulfide.engine.pipeline

import org.lwjgl.opengl.GL20C
import org.lwjgl.opengl.GL43C

class ComputeProgram(src: String) : AutoCloseable {
    val handle: Int = GL20C.glCreateProgram()

    init {
        val shader = GL20C.glCreateShader(GL43C.GL_COMPUTE_SHADER)
        GL20C.glShaderSource(shader, src)
        GL20C.glCompileShader(shader)
        if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0) {
            val log = GL20C.glGetShaderInfoLog(shader)
            GL20C.glDeleteShader(shader)
            error("Compute shader compilation failed:\n$log")
        }

        GL20C.glAttachShader(handle, shader)
        GL20C.glLinkProgram(handle)
        GL20C.glDeleteShader(shader)
        if (GL20C.glGetProgrami(handle, GL20C.GL_LINK_STATUS) == 0) {
            val log = GL20C.glGetProgramInfoLog(handle)
            error("Compute program link failed:\n$log")
        }
    }

    fun uniform(name: String): Int = GL20C.glGetUniformLocation(handle, name)

    fun use() = GL20C.glUseProgram(handle)

    override fun close() = GL20C.glDeleteProgram(handle)
}

