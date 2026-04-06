package graphics.sulfide.engine.pipeline

import org.lwjgl.opengl.GL20C

class ShaderProgram(vertexSrc: String, fragmentSrc: String) : AutoCloseable {
    val handle: Int

    init {
        val vs = compileStage(GL20C.GL_VERTEX_SHADER, vertexSrc, "vertex")
        val fs = compileStage(GL20C.GL_FRAGMENT_SHADER, fragmentSrc, "fragment")

        handle = GL20C.glCreateProgram()
        GL20C.glAttachShader(handle, vs)
        GL20C.glAttachShader(handle, fs)
        GL20C.glLinkProgram(handle)

        if (GL20C.glGetProgrami(handle, GL20C.GL_LINK_STATUS) == 0) {
            val log = GL20C.glGetProgramInfoLog(handle)
            GL20C.glDeleteProgram(handle)
            GL20C.glDeleteShader(vs)
            GL20C.glDeleteShader(fs)
            error("Shader program link failed:\n$log")
        }

        GL20C.glDetachShader(handle, vs)
        GL20C.glDetachShader(handle, fs)
        GL20C.glDeleteShader(vs)
        GL20C.glDeleteShader(fs)
    }

    fun use() {
        GL20C.glUseProgram(handle)
    }

    fun uniformLocation(name: String): Int =
        GL20C.glGetUniformLocation(handle, name)

    override fun close() {
        GL20C.glDeleteProgram(handle)
    }

    companion object {
        fun fromResources(vertexPath: String, fragmentPath: String): ShaderProgram {
            val vs = loadResource(vertexPath)
            val fs = loadResource(fragmentPath)
            return ShaderProgram(vs, fs)
        }

        private fun loadResource(path: String): String {
            val stream = ShaderProgram::class.java.classLoader.getResourceAsStream(path)
                ?: error("Shader resource not found: $path")
            var text = stream.bufferedReader().use { it.readText() }
            if (text.isNotEmpty() && text[0] == '\uFEFF') {
                text = text.substring(1)
            }
            return text
        }

        private fun compileStage(type: Int, src: String, label: String): Int {
            val shader = GL20C.glCreateShader(type)
            GL20C.glShaderSource(shader, src)
            GL20C.glCompileShader(shader)
            if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0) {
                val log = GL20C.glGetShaderInfoLog(shader)
                GL20C.glDeleteShader(shader)
                error("$label shader compilation failed:\n$log")
            }
            return shader
        }
    }
}