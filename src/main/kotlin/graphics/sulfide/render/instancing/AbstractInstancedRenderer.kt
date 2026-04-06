package graphics.sulfide.render.instancing

import graphics.sulfide.engine.buffer.GpuBuffer
import graphics.sulfide.render.entity.EntityModelCollector

abstract class AbstractInstancedRenderer(maxInstances: Int) : AutoCloseable {
    private val capacity: Long = maxInstances.toLong() * EntityModelCollector.INSTANCE_STRIDE

    private val persistentFlags = GpuBuffer.FLAG_MAP_WRITE or
            GpuBuffer.FLAG_MAP_PERSISTENT or
            GpuBuffer.FLAG_MAP_COHERENT

    private val mapFlags = GpuBuffer.MAP_WRITE or
            GpuBuffer.MAP_PERSISTENT or
            GpuBuffer.MAP_COHERENT

    val inputSsbo: GpuBuffer = GpuBuffer(capacity, persistentFlags)

    val inputPtr: Long = inputSsbo.mapRange(0L, capacity, mapFlags)

    private var fence: Long = 0L

    override fun close() {
        inputSsbo.close()
    }
}
