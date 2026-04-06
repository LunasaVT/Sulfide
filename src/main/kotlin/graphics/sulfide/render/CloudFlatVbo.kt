package graphics.sulfide.render

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.system.MemoryUtil

object CloudFlatVbo {
    private const val SCALE = 4.8828125E-4F
    private const val FLOATS_PER_VERT = 5
    private const val STRIDE = FLOATS_PER_VERT * Float.SIZE_BYTES

    const val VERTEX_COUNT = 16 * 16 * 4

    private var vbo = 0
    val isUploaded get() = vbo != 0

    fun upload() {
        check(!isUploaded) { "CloudFlatVbo.upload() called twice" }

        val buf = MemoryUtil.memAllocFloat(VERTEX_COUNT * FLOATS_PER_VERT)
        try {
            for (u in -256 until 256 step 32) {
                for (v in -256 until 256 step 32) {
                    putVert(buf, u, 0f, v + 32, u * SCALE, (v + 32) * SCALE)
                    putVert(buf, u + 32, 0f, v + 32, (u + 32) * SCALE, (v + 32) * SCALE)
                    putVert(buf, u + 32, 0f, v, (u + 32) * SCALE, v * SCALE)
                    putVert(buf, u, 0f, v, u * SCALE, v * SCALE)
                }
            }
            buf.flip()

            vbo = GL15.glGenBuffers()
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW)
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        } finally {
            MemoryUtil.memFree(buf)
        }
    }

    fun draw(cloudY: Float, scrollS: Float, scrollT: Float, r: Float, g: Float, b: Float, alpha: Float) {
        GL11.glColor4f(r, g, b, alpha)

        GL11.glMatrixMode(GL11.GL_TEXTURE)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glTranslatef(scrollS, scrollT, 0f)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)

        GL11.glPushMatrix()
        GL11.glTranslatef(0f, cloudY, 0f)

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY)
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        GL11.glVertexPointer(3, GL11.GL_FLOAT, STRIDE, 0L)
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, STRIDE, 12L)
        GL11.glDrawArrays(GL11.GL_QUADS, 0, VERTEX_COUNT)
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY)
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)

        GL11.glPopMatrix()

        GL11.glMatrixMode(GL11.GL_TEXTURE)
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
    }

    fun destroy() {
        if (isUploaded) {
            GL15.glDeleteBuffers(vbo)
            vbo = 0
        }
    }

    private fun putVert(buf: java.nio.FloatBuffer, x: Int, y: Float, z: Int, u: Float, v: Float) {
        buf.put(x.toFloat()); buf.put(y); buf.put(z.toFloat()); buf.put(u); buf.put(v)
    }
}

