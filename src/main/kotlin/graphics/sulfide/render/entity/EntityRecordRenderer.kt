package graphics.sulfide.render.entity

import graphics.sulfide.engine.command.CommandEncoder
import graphics.sulfide.engine.pipeline.RenderPipelines
import graphics.sulfide.engine.texture.TextureAtlas
import graphics.sulfide.render.entity.EntityModelCollector.INSTANCE_STRIDE
import graphics.sulfide.render.instancing.AbstractInstancedRenderer
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20C
import org.lwjgl.system.MemoryUtil

class EntityRecordRenderer : AbstractInstancedRenderer(MAX_INSTANCES) {
    private var locVP_normal: Int = -1
    private var locOff_normal: Int = -1
    private var locLD0_normal: Int = -1
    private var locLD1_normal: Int = -1
    private var locVP_glint: Int = -1
    private var locOff_glint: Int = -1

    private val vpArray = FloatArray(16)
    private val lightBuf = BufferUtils.createFloatBuffer(4)

    fun render(
        viewProjection: Matrix4f,
        atlas: TextureAtlas,
        lightmapHandle: Int
    ) {
        val normalCount = EntityModelCollector.normalCount
        val glintCount = EntityModelCollector.glintCount
        val totalCount = normalCount + glintCount
        if (totalCount == 0) {
            EntityModelCollector.clear(); return
        }

        if (normalCount > 0) {
            MemoryUtil.memCopy(
                EntityModelCollector.normalPtr(),
                inputPtr,
                normalCount.toLong() * INSTANCE_STRIDE
            )
        }
        if (glintCount > 0) {
            MemoryUtil.memCopy(
                EntityModelCollector.glintPtr(),
                inputPtr + normalCount.toLong() * INSTANCE_STRIDE,
                glintCount.toLong() * INSTANCE_STRIDE
            )
        }

        viewProjection.get(vpArray)

        lightBuf.clear()
        GL11.glGetLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION, lightBuf)
        val l0x = lightBuf.get(0);
        val l0y = lightBuf.get(1);
        val l0z = lightBuf.get(2)
        lightBuf.clear()
        GL11.glGetLightfv(GL11.GL_LIGHT1, GL11.GL_POSITION, lightBuf)
        val l1x = lightBuf.get(0);
        val l1y = lightBuf.get(1);
        val l1z = lightBuf.get(2)

        if (normalCount > 0) {
            CommandEncoder().use { enc ->
                val pipe = RenderPipelines.ENTITY_INSTANCED
                enc.setPipeline(pipe)

                inputSsbo.bindAsSSBO(0)
                enc.setSampler(0, atlas.handle)
                enc.setSampler(1, lightmapHandle)

                if (locVP_normal < 0) locVP_normal = pipe.uniform("uViewProjection")
                if (locOff_normal < 0) locOff_normal = pipe.uniform("uInstanceOffset")
                if (locLD0_normal < 0) locLD0_normal = pipe.uniform("uLightDir0")
                if (locLD1_normal < 0) locLD1_normal = pipe.uniform("uLightDir1")

                enc.setUniformMat4(locVP_normal, vpArray)
                enc.setUniform1i(locOff_normal, 0)
                enc.setUniform3f(locLD0_normal, l0x, l0y, l0z)
                enc.setUniform3f(locLD1_normal, l1x, l1y, l1z)

                enc.setVertexBuffer(EntityCube.vertexBuffer, stride = EntityCube.VERTEX_STRIDE)
                enc.setIndexBuffer(EntityCube.indexBuffer)

                enc.drawIndexed(EntityCube.INDEX_COUNT, normalCount)
            }
        }

        if (glintCount > 0) {
            CommandEncoder().use { enc ->
                val pipe = RenderPipelines.ENTITY_INSTANCED_GLINT
                enc.setPipeline(pipe)

                inputSsbo.bindAsSSBO(0)
                enc.setSampler(0, atlas.handle)
                enc.setSampler(1, lightmapHandle)

                if (locVP_glint < 0) locVP_glint = pipe.uniform("uViewProjection")
                if (locOff_glint < 0) locOff_glint = pipe.uniform("uInstanceOffset")
                enc.setUniformMat4(locVP_glint, vpArray)
                enc.setUniform1i(locOff_glint, normalCount)

                enc.setVertexBuffer(EntityCube.vertexBuffer, stride = EntityCube.VERTEX_STRIDE)
                enc.setIndexBuffer(EntityCube.indexBuffer)

                enc.drawIndexed(EntityCube.INDEX_COUNT, glintCount)
            }
        }

        EntityModelCollector.clear()
    }

    companion object {
        const val MAX_INSTANCES = 32_384
    }
}