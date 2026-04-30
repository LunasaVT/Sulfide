package graphics.sulfide.engine

import org.lwjgl.BufferUtils

object LightState {
    val light0 = BufferUtils.createFloatBuffer(4)
    val light1 = BufferUtils.createFloatBuffer(4)
}