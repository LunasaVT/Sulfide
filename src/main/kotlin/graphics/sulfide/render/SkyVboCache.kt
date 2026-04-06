package graphics.sulfide.render

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

object SkyVboCache {
    private const val STRIDE = 20
    private const val FLOATS_PER_VERT = 5
    private const val TEX_OFFSET = 12L

    private const val SUN_FIRST = 0
    private const val MOON_FIRST = 4
    private const val END_FIRST = 36
    private const val END_COUNT = 24
    private const val TOTAL_VERTS = 60

    private var vbo = 0

    @get:JvmStatic
    val isUploaded get() = vbo != 0

    @JvmStatic
    fun upload() {
        check(vbo == 0) { "SkyVboCache already uploaded" }

        val buf = MemoryUtil.memAllocFloat(TOTAL_VERTS * FLOATS_PER_VERT)  // 300 floats
        try {
            buildSun(buf)
            for (phase in 0 until 8) buildMoon(buf, phase)
            buildEndSky(buf)
            buf.flip()

            vbo = GL15.glGenBuffers()
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW)
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        } finally {
            MemoryUtil.memFree(buf)
        }
    }

    @JvmStatic
    fun destroy() {
        if (vbo != 0) {
            GL15.glDeleteBuffers(vbo)
            vbo = 0
        }
    }

    @JvmStatic
    fun bindSky() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY)
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        GL11.glVertexPointer(3, GL11.GL_FLOAT, STRIDE, 0L)
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, STRIDE, TEX_OFFSET)
    }

    @JvmStatic
    fun unbindSky() {
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
    }

    @JvmStatic
    fun drawSun() {
        GL11.glDrawArrays(GL11.GL_QUADS, SUN_FIRST, 4)
    }

    @JvmStatic
    fun drawMoon(phase: Int) {
        GL11.glDrawArrays(GL11.GL_QUADS, MOON_FIRST + (phase and 7) * 4, 4)
    }

    @JvmStatic
    fun drawEndSky() {
        GL11.glDisable(GL11.GL_FOG)
        GL11.glDisable(GL11.GL_ALPHA_TEST)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDepthMask(false)
        GL11.glColor4f(40f / 255f, 40f / 255f, 40f / 255f, 1f)

        bindSky()
        GL11.glDrawArrays(GL11.GL_QUADS, END_FIRST, END_COUNT)
        unbindSky()

        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
    }

    private fun buildSun(buf: FloatBuffer) {
        putQuad(
            buf,
            -30f, 100f, -30f, 0f, 0f,
            30f, 100f, -30f, 1f, 0f,
            30f, 100f, 30f, 1f, 1f,
            -30f, 100f, 30f, 0f, 1f
        )
    }

    private fun buildMoon(buf: FloatBuffer, phase: Int) {
        val col = phase and 3
        val row = (phase shr 2) and 1
        val u0 = col * 0.25f
        val v0 = row * 0.5f
        val u1 = u0 + 0.25f
        val v1 = v0 + 0.5f
        putQuad(
            buf,
            -20f, -100f, 20f, u1, v1,
            20f, -100f, 20f, u0, v1,
            20f, -100f, -20f, u0, v0,
            -20f, -100f, -20f, u1, v0
        )
    }

    private fun buildEndSky(buf: FloatBuffer) {
        val us = floatArrayOf(0f, 0f, 16f, 16f)
        val vs = floatArrayOf(0f, 16f, 16f, 0f)
        val faces = arrayOf(
            floatArrayOf(-100f, -100f, -100f, -100f, -100f, 100f, 100f, -100f, 100f, 100f, -100f, -100f),
            floatArrayOf(-100f, 100f, -100f, -100f, -100f, -100f, 100f, -100f, -100f, 100f, 100f, -100f),
            floatArrayOf(-100f, -100f, 100f, -100f, 100f, 100f, 100f, 100f, 100f, 100f, -100f, 100f),
            floatArrayOf(-100f, 100f, 100f, -100f, 100f, -100f, 100f, 100f, -100f, 100f, 100f, 100f),
            floatArrayOf(100f, -100f, -100f, 100f, -100f, 100f, 100f, 100f, 100f, 100f, 100f, -100f),
            floatArrayOf(-100f, 100f, -100f, -100f, 100f, 100f, -100f, -100f, 100f, -100f, -100f, -100f)
        )
        for (face in faces) {
            for (vi in 0..3) {
                val i3 = vi * 3
                buf.put(face[i3]).put(face[i3 + 1]).put(face[i3 + 2])
                buf.put(us[vi]).put(vs[vi])
            }
        }
    }

    private fun putQuad(
        buf: FloatBuffer,
        x0: Float, y0: Float, z0: Float, u0: Float, v0: Float,
        x1: Float, y1: Float, z1: Float, u1: Float, v1: Float,
        x2: Float, y2: Float, z2: Float, u2: Float, v2: Float,
        x3: Float, y3: Float, z3: Float, u3: Float, v3: Float
    ) {
        buf.put(x0).put(y0).put(z0).put(u0).put(v0)
            .put(x1).put(y1).put(z1).put(u1).put(v1)
            .put(x2).put(y2).put(z2).put(u2).put(v2)
            .put(x3).put(y3).put(z3).put(u3).put(v3)
    }
}