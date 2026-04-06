package graphics.sulfide.engine.pipeline

import org.lwjgl.opengl.GL11C

class VertexAttribute(
    val index: Int,
    val componentCount: Int,
    val type: Int = GL11C.GL_FLOAT,
    val normalized: Boolean = false,
    val relativeOffset: Int = 0
)