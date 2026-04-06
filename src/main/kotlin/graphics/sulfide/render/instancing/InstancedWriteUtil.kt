package graphics.sulfide.render.instancing

import sun.misc.Unsafe

object InstancedWriteUtil {
    @PublishedApi
    internal val UNSAFE: Unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").let { f ->
        f.isAccessible = true
        f.get(null) as Unsafe
    }

    @JvmStatic
    fun writeMat4(ptr: Long, src: FloatArray, srcOffset: Int) {
        for (i in 0 until 16) {
            UNSAFE.putFloat(ptr + (i.toLong() shl 2), src[srcOffset + i])
        }
    }

    @JvmStatic
    inline fun putFloat(ptr: Long, value: Float) = UNSAFE.putFloat(ptr, value)

    @JvmStatic
    inline fun putInt(ptr: Long, value: Int) = UNSAFE.putInt(ptr, value)

    @JvmStatic
    inline fun putLong(ptr: Long, value: Long) = UNSAFE.putLong(ptr, value)

    @JvmStatic
    inline fun putFloatBits(ptr: Long, value: Float) = UNSAFE.putInt(ptr, java.lang.Float.floatToRawIntBits(value))
}

