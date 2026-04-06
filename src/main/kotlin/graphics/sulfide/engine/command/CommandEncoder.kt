package graphics.sulfide.engine.command

import graphics.sulfide.engine.buffer.GpuBuffer
import graphics.sulfide.engine.pipeline.Pipeline
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL20C
import org.lwjgl.opengl.GL42C
import org.lwjgl.opengl.GL45C

class CommandEncoder : AutoCloseable {
    private var activePipeline: Pipeline? = null

    fun setPipeline(pipeline: Pipeline) {
        activePipeline = pipeline
        pipeline.bind()
    }

    fun setVertexBuffer(buffer: GpuBuffer, offset: Long = 0, stride: Int) {
        val pipeline = requirePipeline()
        GL45C.glVertexArrayVertexBuffer(pipeline.vao, 0, buffer.handle, offset, stride)
    }

    fun setIndexBuffer(buffer: GpuBuffer) {
        val pipeline = requirePipeline()
        GL45C.glVertexArrayElementBuffer(pipeline.vao, buffer.handle)
    }

    fun bindShaderStorage(buffer: GpuBuffer, bindingPoint: Int) {
        buffer.bindAsSSBO(bindingPoint)
    }

    fun setSampler(unit: Int, textureHandle: Int) {
        GL45C.glBindTextureUnit(unit, textureHandle)
    }

    fun setUniformMat4(location: Int, matrix: FloatArray) {
        GL20C.glUniformMatrix4fv(location, false, matrix)
    }

    fun setUniform1i(location: Int, value: Int) {
        GL20C.glUniform1i(location, value)
    }

    fun setUniform3f(location: Int, x: Float, y: Float, z: Float) {
        GL20C.glUniform3f(location, x, y, z)
    }

    fun drawIndexed(
        indexCount: Int,
        instanceCount: Int,
        firstIndex: Int = 0,
        baseVertex: Int = 0,
        baseInstance: Int = 0
    ) {
        GL42C.glDrawElementsInstancedBaseVertexBaseInstance(
            GL11C.GL_TRIANGLES,
            indexCount,
            GL11C.GL_UNSIGNED_SHORT,
            (firstIndex * 2).toLong(),
            instanceCount,
            baseVertex,
            baseInstance
        )
    }

    fun drawArraysInstanced(vertexCount: Int, instanceCount: Int, baseInstance: Int = 0) {
        GL42C.glDrawArraysInstancedBaseInstance(
            GL11C.GL_TRIANGLES, 0, vertexCount, instanceCount, baseInstance
        )
    }

    override fun close() {
        reset()
    }

    fun reset() {
        GL45C.glBindVertexArray(0)
        GL20C.glUseProgram(0)
        activePipeline = null
    }

    private fun requirePipeline(): Pipeline =
        activePipeline ?: error("A pipeline must be bound to perform this action.")
}
