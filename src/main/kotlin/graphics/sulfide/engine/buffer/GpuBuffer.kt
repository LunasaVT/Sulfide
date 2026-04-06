package graphics.sulfide.engine.buffer

import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL44C
import org.lwjgl.opengl.GL45C

class GpuBuffer(val size: Long, flags: Int) : AutoCloseable {
    val handle: Int = GL45C.glCreateBuffers()

    init {
        GL45C.glNamedBufferStorage(handle, size, flags)
    }

    fun mapRange(offset: Long, length: Long, access: Int): Long {
        val ptr = GL45C.nglMapNamedBufferRange(handle, offset, length, access)
        check(ptr != 0L) { "glMapNamedBufferRange failed for buffer $handle (size=$size)" }
        return ptr
    }

    fun unmap(): Boolean = GL45C.glUnmapNamedBuffer(handle)

    fun subData(destOffset: Long, src: Long, dataSize: Long) {
        GL45C.nglNamedBufferSubData(handle, destOffset, dataSize, src)
    }

    fun bindAsSSBO(bindingPoint: Int) {
        GL30C.glBindBufferBase(GL45C.GL_SHADER_STORAGE_BUFFER, bindingPoint, handle)
    }

    override fun close() {
        GL15C.glDeleteBuffers(handle)
    }

    companion object {
        const val FLAG_MAP_WRITE: Int = GL44C.GL_MAP_WRITE_BIT

        const val FLAG_DYNAMIC: Int = GL44C.GL_DYNAMIC_STORAGE_BIT

        const val FLAG_MAP_PERSISTENT: Int = GL44C.GL_MAP_PERSISTENT_BIT

        const val FLAG_MAP_COHERENT: Int = GL44C.GL_MAP_COHERENT_BIT

        const val MAP_WRITE: Int = GL30C.GL_MAP_WRITE_BIT

        const val MAP_INVALIDATE: Int = GL30C.GL_MAP_INVALIDATE_BUFFER_BIT

        const val MAP_PERSISTENT: Int = GL44C.GL_MAP_PERSISTENT_BIT

        const val MAP_COHERENT: Int = GL44C.GL_MAP_COHERENT_BIT
    }
}