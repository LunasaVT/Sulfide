package graphics.sulfide.engine.texture

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL13C
import org.lwjgl.opengl.GL30C
import org.lwjgl.opengl.GL45C
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

class TextureAtlas(
    val layerSize: Int = DEFAULT_LAYER_SIZE,
    private val maxLayers: Int = DEFAULT_MAX_LAYERS
) : AutoCloseable {
    val handle: Int = GL45C.glCreateTextures(GL30C.GL_TEXTURE_2D_ARRAY)

    var activeLayerCount: Int = 0
        private set

    private val cache = Object2ObjectOpenHashMap<String, TextureRegion>(64)

    private val shelves: MutableList<MutableList<Shelf>> = ArrayList(maxLayers)

    init {
        GL45C.glTextureStorage3D(
            handle,
            1,
            GL11C.GL_RGBA8,
            layerSize, layerSize,
            maxLayers
        )
        var err = GL11C.glGetError()
        check(err == GL11C.GL_NO_ERROR) { "glTextureStorage3D failed: GL error 0x${err.toString(16)}" }

        GL45C.glTextureParameteri(handle, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST)
        GL45C.glTextureParameteri(handle, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST)

        GL45C.glTextureParameteri(handle, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE)
        GL45C.glTextureParameteri(handle, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE)

        err = GL11C.glGetError()
        check(err == GL11C.GL_NO_ERROR) { "glTextureParameteri failed: GL error 0x${err.toString(16)}" }
    }

    fun upload(width: Int, height: Int, pixels: ByteBuffer): TextureRegion {
        require(width in 1..layerSize && height in 1..layerSize) {
            "Texture ($width×$height) exceeds atlas layer size ($layerSize)"
        }
        val region = allocateRegion(width, height)
        GL45C.glTextureSubImage3D(
            handle,
            0,
            region.x,
            region.y,
            region.layer,
            width, height, 1,
            GL11C.GL_RGBA,
            GL11C.GL_UNSIGNED_BYTE,
            pixels
        )
        val err = GL11C.glGetError()
        check(err == GL11C.GL_NO_ERROR) { "glTextureSubImage3D failed: GL error 0x${err.toString(16)}" }
        return region
    }

    fun upload(width: Int, height: Int, rgbaPixels: ByteArray): TextureRegion {
        val buf = MemoryUtil.memAlloc(rgbaPixels.size)
        try {
            buf.put(rgbaPixels).flip()
            return upload(width, height, buf)
        } finally {
            MemoryUtil.memFree(buf)
        }
    }

    fun uploadCached(key: String, width: Int, height: Int, pixelSupplier: () -> ByteArray): TextureRegion {
        cache[key]?.let { return it }
        val region = upload(width, height, pixelSupplier())
        cache[key] = region
        return region
    }

    fun getCached(key: String): TextureRegion? = cache[key]

    fun uploadFromTexture(
        width: Int,
        height: Int,
        srcHandle: Int
    ): TextureRegion {
        require(width in 1..layerSize && height in 1..layerSize) {
            "Texture ($width×$height) exceeds atlas layer size ($layerSize)"
        }

        val buf = MemoryUtil.memAlloc(width * height * 4)
        try {
            GL45C.glGetTextureImage(srcHandle, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, buf)

            val region = allocateRegion(width, height)
            GL45C.glTextureSubImage3D(
                handle, 0,
                region.x, region.y, region.layer,
                width, height, 1,
                GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE,
                buf
            )
            return region
        } finally {
            MemoryUtil.memFree(buf)
        }
    }

    fun uploadCachedFromTexture(
        key: String,
        width: Int,
        height: Int,
        srcHandle: Int
    ): TextureRegion {
        cache[key]?.let { return it }
        val region = uploadFromTexture(width, height, srcHandle)
        cache[key] = region
        return region
    }

    private fun allocateRegion(width: Int, height: Int): TextureRegion {
        for (layerIdx in 0 until shelves.size) {
            val region = tryAllocateInLayer(layerIdx, width, height)
            if (region != null) return region
        }
        check(shelves.size < maxLayers) {
            "all $maxLayers layers are full, cannot allocate ${width}×$height"
        }
        val newLayerIdx = shelves.size
        shelves.add(ArrayList())
        activeLayerCount = shelves.size
        return tryAllocateInLayer(newLayerIdx, width, height)
            ?: error("Failed to allocate ${width}×$height in a fresh layer (should never happen)")
    }

    private fun tryAllocateInLayer(layerIdx: Int, width: Int, height: Int): TextureRegion? {
        val layerShelves = shelves[layerIdx]

        for (shelf in layerShelves) {
            if (height <= shelf.height && shelf.cursorX + width <= layerSize) {
                val region = TextureRegion(layerIdx, shelf.cursorX, shelf.y, width, height, layerSize)
                shelf.cursorX += width
                return region
            }
        }

        val nextY = if (layerShelves.isEmpty()) 0 else {
            val last = layerShelves.last()
            last.y + last.height
        }
        if (nextY + height > layerSize) return null

        val newShelf = Shelf(y = nextY, height = height, cursorX = width)
        layerShelves.add(newShelf)
        return TextureRegion(layerIdx, 0, nextY, width, height, layerSize)
    }

    data class Shelf(
        val y: Int,
        val height: Int,
        var cursorX: Int
    )

    override fun close() {
        GL11C.glDeleteTextures(handle)
    }

    companion object {
        const val DEFAULT_LAYER_SIZE: Int = 1024
        const val DEFAULT_MAX_LAYERS: Int = 16
    }
}

