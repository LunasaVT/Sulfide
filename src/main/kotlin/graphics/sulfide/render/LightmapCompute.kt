package graphics.sulfide.render

import graphics.sulfide.engine.pipeline.ComputeProgram
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL20C
import org.lwjgl.opengl.GL42C
import org.lwjgl.opengl.GL43C

class LightmapCompute : AutoCloseable {
    private val program = ComputeProgram(
        javaClass.classLoader
            .getResourceAsStream("shaders/lightmap.comp")!!
            .bufferedReader().use { it.readText() }
    )

    private var locBrightness = -1
    private var locSkyFactor = -1
    private var locSkyMul = -1
    private var locFlicker = -1
    private var locSkyDark = -1
    private var locNightVision = -1
    private var locGamma = -1
    private var locDimType = -1
    private var locLightning = -1

    private fun loc(cached: Int, name: String): Int =
        if (cached >= 0) cached else program.uniform(name)

    fun dispatch(
        targetGlTex: Int,
        brightness: FloatArray,
        skyFactor: Float,
        flicker: Float,
        skyDark: Float,
        nightVision: Float,
        gamma: Float,
        dimType: Int,
        lightning: Boolean
    ) {
        GL42C.glBindImageTexture(
            0, targetGlTex, 0, false, 0,
            GL42C.GL_WRITE_ONLY, GL11C.GL_RGBA8
        )

        program.use()

        if (locBrightness < 0) locBrightness = loc(locBrightness, "uBrightness")
        if (locSkyFactor < 0) locSkyFactor = loc(locSkyFactor, "uSkyFactor")
        if (locSkyMul < 0) locSkyMul = loc(locSkyMul, "uSkyMul")
        if (locFlicker < 0) locFlicker = loc(locFlicker, "uFlicker")
        if (locSkyDark < 0) locSkyDark = loc(locSkyDark, "uSkyDark")
        if (locNightVision < 0) locNightVision = loc(locNightVision, "uNightVision")
        if (locGamma < 0) locGamma = loc(locGamma, "uGamma")
        if (locDimType < 0) locDimType = loc(locDimType, "uDimType")
        if (locLightning < 0) locLightning = loc(locLightning, "uLightning")

        GL20C.glUniform1fv(locBrightness, brightness)
        GL20C.glUniform1f(locSkyFactor, skyFactor)
        GL20C.glUniform1f(locSkyMul, skyFactor * 0.95f + 0.05f)
        GL20C.glUniform1f(locFlicker, flicker)
        GL20C.glUniform1f(locSkyDark, skyDark)
        GL20C.glUniform1f(locNightVision, nightVision)
        GL20C.glUniform1f(locGamma, gamma)
        GL20C.glUniform1i(locDimType, dimType)
        GL20C.glUniform1i(locLightning, if (lightning) 1 else 0)

        GL43C.glDispatchCompute(1, 1, 1)

        GL42C.glMemoryBarrier(
            GL42C.GL_TEXTURE_FETCH_BARRIER_BIT or
                    GL42C.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT
        )

        GL20C.glUseProgram(0)
        GL42C.glBindImageTexture(0, 0, 0, false, 0, GL42C.GL_WRITE_ONLY, GL11C.GL_RGBA8)
    }

    override fun close() = program.close()
}