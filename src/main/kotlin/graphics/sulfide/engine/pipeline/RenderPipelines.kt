package graphics.sulfide.engine.pipeline

import graphics.sulfide.render.entity.EntityCube
import org.lwjgl.opengl.GL11C

object RenderPipelines {
    lateinit var ENTITY_INSTANCED: Pipeline
        private set

    lateinit var ENTITY_INSTANCED_GLINT: Pipeline
        private set

    var isInitialised: Boolean = false
        private set

    fun init() {
        check(!isInitialised) { "RenderPipelines.init() called twice" }

        val vertexFormat = EntityCube.VERTEX_FORMAT

        ENTITY_INSTANCED = Pipeline(
            shader = ShaderProgram.fromResources(
                "shaders/entity_instanced.vert",
                "shaders/entity_instanced.frag"
            ),
            state = PipelineState(
                depthTest = true,
                depthFunc = GL11C.GL_LEQUAL,
                depthMask = true,
                cullFace = true,
                blend = true,
                blendSrc = GL11C.GL_SRC_ALPHA,
                blendDst = GL11C.GL_ONE_MINUS_SRC_ALPHA
            ),
            vertexFormat = vertexFormat
        )

        ENTITY_INSTANCED_GLINT = Pipeline(
            shader = ShaderProgram.fromResources(
                "shaders/entity_instanced_glint.vert",
                "shaders/entity_instanced_glint.frag"
            ),
            state = PipelineState(
                depthTest = true,
                depthFunc = GL11C.GL_EQUAL,
                depthMask = false,
                cullFace = false,
                blend = true,
                blendSrc = GL11C.GL_SRC_COLOR,
                blendDst = GL11C.GL_ONE
            ),
            vertexFormat = vertexFormat
        )

        isInitialised = true
    }

    fun destroy() {
        if (isInitialised) {
            ENTITY_INSTANCED.close()
            ENTITY_INSTANCED_GLINT.close()
            isInitialised = false
        }
    }
}