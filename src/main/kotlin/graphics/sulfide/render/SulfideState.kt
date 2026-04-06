package graphics.sulfide.render

import graphics.sulfide.config.SulfideOptionStorage
import graphics.sulfide.engine.pipeline.RenderPipelines
import graphics.sulfide.engine.texture.TextureAtlas
import graphics.sulfide.engine.texture.TextureRegion
import graphics.sulfide.render.entity.EntityCube
import graphics.sulfide.render.entity.EntityRecordRenderer
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL13C
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL45C

object SulfideState {
    lateinit var atlas: TextureAtlas
    lateinit var renderer: EntityRecordRenderer
    lateinit var lightmapCompute: LightmapCompute
        private set

    @Volatile
    var recording: Boolean = false
        private set

    /**
     * When true, [graphics.sulfide.mixin.MixinModelPart] will skip recording and fall through to
     * vanilla GL rendering.  Set by the held-item-renderer mixin so that
     * block-entity held items (chests, heads, ...) bypass instancing without
     * flushing the current batch.
     */
    @JvmField
    @Volatile
    var suppressRecording: Boolean = false

    val isInitialized: Boolean get() = ::renderer.isInitialized

    @JvmStatic
    fun init() {
        val config = SulfideOptionStorage.getInstance()
        EntityCube.upload()
        RenderPipelines.init()
        atlas = TextureAtlas(config.atlasLayerSize, config.maxAtlasLayers)
        renderer = EntityRecordRenderer()
        lightmapCompute = LightmapCompute()
    }

    @JvmStatic
    fun destroy() {
        recording = false
        if (isInitialized) {
            renderer.close()
            atlas.close()
            RenderPipelines.destroy()
            EntityCube.destroy()
            lightmapCompute.close()
        }
        if (CloudFlatVbo.isUploaded) CloudFlatVbo.destroy()
        if (SkyVboCache.isUploaded) SkyVboCache.destroy()
    }

    fun beginFrame() {
        if (!isInitialized) return
        recording = true
    }

    fun endFrame(projection: Matrix4f) {
        recording = false
        if (!isInitialized) return

        val prevDepthTest = GL11C.glIsEnabled(GL11C.GL_DEPTH_TEST)
        val prevDepthFunc = GL11C.glGetInteger(GL11C.GL_DEPTH_FUNC)
        val prevDepthMask = GL11C.glGetBoolean(GL11C.GL_DEPTH_WRITEMASK)
        val prevCullFace = GL11C.glIsEnabled(GL11C.GL_CULL_FACE)
        val prevCullMode = GL11C.glGetInteger(GL11C.GL_CULL_FACE_MODE)
        val prevBlend = GL11C.glIsEnabled(GL11C.GL_BLEND)
        val prevBlendSrc = GL11C.glGetInteger(GL11C.GL_BLEND_SRC)
        val prevBlendDst = GL11C.glGetInteger(GL11C.GL_BLEND_DST)

        val prevActiveTex = GL11C.glGetInteger(GL13C.GL_ACTIVE_TEXTURE)
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0)
        val prevTex0 = GL11C.glGetInteger(GL11C.GL_TEXTURE_BINDING_2D)
        GL13C.glActiveTexture(GL13C.GL_TEXTURE1)
        val prevTex1 = GL11C.glGetInteger(GL11C.GL_TEXTURE_BINDING_2D)
        GL13C.glActiveTexture(prevActiveTex)

        renderer.render(projection, atlas, prevTex1)

        GL13C.glActiveTexture(GL13C.GL_TEXTURE0)
        GL11C.glBindTexture(GL30C.GL_TEXTURE_2D_ARRAY, 0)
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, prevTex0)
        GL13C.glActiveTexture(GL13C.GL_TEXTURE1)
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, prevTex1)
        GL13C.glActiveTexture(prevActiveTex)

        if (prevDepthTest) GL11C.glEnable(GL11C.GL_DEPTH_TEST) else GL11C.glDisable(GL11C.GL_DEPTH_TEST)
        GL11C.glDepthFunc(prevDepthFunc)
        GL11C.glDepthMask(prevDepthMask)
        if (prevCullFace) GL11C.glEnable(GL11C.GL_CULL_FACE) else GL11C.glDisable(GL11C.GL_CULL_FACE)
        GL11C.glCullFace(prevCullMode)
        if (prevBlend) GL11C.glEnable(GL11C.GL_BLEND) else GL11C.glDisable(GL11C.GL_BLEND)
        GL11C.glBlendFunc(prevBlendSrc, prevBlendDst)
    }

    fun getOrUploadTexture(glTexId: Int): TextureRegion? {
        if (!isInitialized || glTexId <= 0) return null
        val key = glTexId.toString()
        atlas.getCached(key)?.let { return it }
        val w = GL45C.glGetTextureLevelParameteri(glTexId, 0, GL11C.GL_TEXTURE_WIDTH)
        val h = GL45C.glGetTextureLevelParameteri(glTexId, 0, GL11C.GL_TEXTURE_HEIGHT)
        if (w <= 0 || h <= 0) return null
        return atlas.uploadCachedFromTexture(key, w, h, glTexId)
    }
}