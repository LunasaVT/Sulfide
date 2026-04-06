package graphics.sulfide.engine.pipeline

import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL14C

class PipelineState(
    val depthTest: Boolean = true,
    val depthFunc: Int = GL11C.GL_LEQUAL,
    val depthMask: Boolean = true,
    val cullFace: Boolean = true,
    val cullMode: Int = GL11C.GL_BACK,
    val blend: Boolean = false,
    val blendSrc: Int = GL11C.GL_SRC_ALPHA,
    val blendDst: Int = GL11C.GL_ONE_MINUS_SRC_ALPHA
) {
    fun apply() {
        if (depthTest) {
            GL11C.glEnable(GL11C.GL_DEPTH_TEST)
            GL11C.glDepthFunc(depthFunc)
        } else {
            GL11C.glDisable(GL11C.GL_DEPTH_TEST)
        }
        GL11C.glDepthMask(depthMask)

        if (cullFace) {
            GL11C.glEnable(GL11C.GL_CULL_FACE)
            GL11C.glCullFace(cullMode)
        } else {
            GL11C.glDisable(GL11C.GL_CULL_FACE)
        }

        if (blend) {
            GL11C.glEnable(GL11C.GL_BLEND)
            GL14C.glBlendFuncSeparate(blendSrc, blendDst, GL11C.GL_ONE, GL11C.GL_ZERO)
        } else {
            GL11C.glDisable(GL11C.GL_BLEND)
        }
    }
}