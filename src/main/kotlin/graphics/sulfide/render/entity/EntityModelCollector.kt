package graphics.sulfide.render.entity

import graphics.sulfide.engine.texture.TextureRegion
import graphics.sulfide.render.instancing.InstancedWriteUtil.putFloat
import graphics.sulfide.render.instancing.InstancedWriteUtil.putInt
import org.joml.Matrix4f
import org.lwjgl.system.MemoryUtil

object EntityModelCollector {
    const val INSTANCE_STRIDE: Long = 176L
    private const val INITIAL_CAP = 4096

    private var normalBuf: Long = MemoryUtil.nmemAllocChecked(INITIAL_CAP * INSTANCE_STRIDE)
    private var normalCap: Int = INITIAL_CAP

    var normalCount: Int = 0
        private set

    private var glintBuf: Long = MemoryUtil.nmemAllocChecked(INITIAL_CAP * INSTANCE_STRIDE)
    private var glintCap: Int = INITIAL_CAP

    var glintCount: Int = 0
        private set

    val recordCount: Int get() = normalCount + glintCount

    var enabled: Boolean = true

    @JvmStatic
    @JvmOverloads
    fun record(
        transform: Matrix4f,
        packedUvRects: IntArray,
        region: TextureRegion,
        lightU: Int,
        lightV: Int,
        packedColor: Int,
        overlayColor: Int = 0,
        uvM00: Float = 1f, uvM01: Float = 0f,
        uvM10: Float = 0f, uvM11: Float = 1f,
        uvM30: Float = 0f, uvM31: Float = 0f,
        isGlint: Boolean = false
    ) {
        if (!enabled) return

        if (isGlint) {
            if (glintCount >= glintCap) growGlint()
            writeInstance(
                glintBuf + glintCount.toLong() * INSTANCE_STRIDE,
                transform, uvM00, uvM01, uvM10, uvM11, uvM30, uvM31,
                packedUvRects, lightU, lightV, packedColor, overlayColor, region
            )
            glintCount++
        } else {
            if (normalCount >= normalCap) growNormal()
            writeInstance(
                normalBuf + normalCount.toLong() * INSTANCE_STRIDE,
                transform, uvM00, uvM01, uvM10, uvM11, uvM30, uvM31,
                packedUvRects, lightU, lightV, packedColor, overlayColor, region
            )
            normalCount++
        }
    }

    fun normalPtr(): Long = normalBuf
    fun glintPtr(): Long = glintBuf

    @JvmStatic
    fun clear() {
        normalCount = 0
        glintCount = 0
    }

    @JvmStatic
    fun destroy() {
        MemoryUtil.nmemFree(normalBuf); normalBuf = 0L
        MemoryUtil.nmemFree(glintBuf); glintBuf = 0L
    }

    @JvmStatic
    fun packUV(u: Float, v: Float): Int {
        val ui = (u.coerceIn(0f, 1f) * 65535f + 0.5f).toInt() and 0xFFFF
        val vi = (v.coerceIn(0f, 1f) * 65535f + 0.5f).toInt() and 0xFFFF
        return ui or (vi shl 16)
    }

    @JvmStatic
    fun packRGBA(r: Int, g: Int, b: Int, a: Int): Int =
        (r and 0xFF) or ((g and 0xFF) shl 8) or ((b and 0xFF) shl 16) or ((a and 0xFF) shl 24)

    private fun writeInstance(
        ptr: Long,
        m: Matrix4f,
        uvM00: Float, uvM01: Float,
        uvM10: Float, uvM11: Float,
        uvM30: Float, uvM31: Float,
        uvRects: IntArray,
        lightU: Int, lightV: Int, packedColor: Int,
        overlayColor: Int,
        region: TextureRegion
    ) {
        putFloat(ptr + 0L, m.m00())
        putFloat(ptr + 4L, m.m01())
        putFloat(ptr + 8L, m.m02())
        putFloat(ptr + 12L, m.m03())
        putFloat(ptr + 16L, m.m10())
        putFloat(ptr + 20L, m.m11())
        putFloat(ptr + 24L, m.m12())
        putFloat(ptr + 28L, m.m13())
        putFloat(ptr + 32L, m.m20())
        putFloat(ptr + 36L, m.m21())
        putFloat(ptr + 40L, m.m22())
        putFloat(ptr + 44L, m.m23())
        putFloat(ptr + 48L, m.m30())
        putFloat(ptr + 52L, m.m31())
        putFloat(ptr + 56L, m.m32())
        putFloat(ptr + 60L, m.m33())

        putFloat(ptr + 64L, uvM00)
        putFloat(ptr + 68L, uvM01)
        putFloat(ptr + 72L, uvM10)
        putFloat(ptr + 76L, uvM11)
        putFloat(ptr + 80L, uvM30)
        putFloat(ptr + 84L, uvM31)

        putInt(ptr + 88L, overlayColor)
        putInt(ptr + 92L, 0)

        putInt(ptr + 96L, uvRects[0])
        putInt(ptr + 100L, uvRects[1])
        putInt(ptr + 104L, uvRects[2])
        putInt(ptr + 108L, uvRects[3])
        putInt(ptr + 112L, uvRects[4])
        putInt(ptr + 116L, uvRects[5])
        putInt(ptr + 120L, uvRects[6])
        putInt(ptr + 124L, uvRects[7])
        putInt(ptr + 128L, uvRects[8])
        putInt(ptr + 132L, uvRects[9])
        putInt(ptr + 136L, uvRects[10])
        putInt(ptr + 140L, uvRects[11])

        putInt(ptr + 144L, lightU)
        putInt(ptr + 148L, lightV)
        putInt(ptr + 152L, packedColor)
        putInt(ptr + 156L, region.layer)
        putFloat(ptr + 160L, region.u0)
        putFloat(ptr + 164L, region.v0)
        putFloat(ptr + 168L, region.uScale)
        putFloat(ptr + 172L, region.vScale)
    }

    private fun growNormal() {
        normalCap = normalCap shl 1
        normalBuf = MemoryUtil.nmemReallocChecked(normalBuf, normalCap.toLong() * INSTANCE_STRIDE)
    }

    private fun growGlint() {
        glintCap = glintCap shl 1
        glintBuf = MemoryUtil.nmemReallocChecked(glintBuf, glintCap.toLong() * INSTANCE_STRIDE)
    }
}