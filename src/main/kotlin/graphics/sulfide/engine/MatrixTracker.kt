package graphics.sulfide.engine

import org.joml.Matrix4f
import java.nio.FloatBuffer
import java.util.IdentityHashMap
import kotlin.collections.ArrayDeque

object MatrixTracker {
    private const val GL_MODELVIEW = 0x1700
    private const val GL_PROJECTION = 0x1701
    private const val GL_TEXTURE = 0x1702

    private val modelStack = ArrayDeque<Matrix4f>().also { it.addLast(Matrix4f()) }
    private val projStack = ArrayDeque<Matrix4f>().also { it.addLast(Matrix4f()) }
    private val texStack = ArrayDeque<Matrix4f>().also { it.addLast(Matrix4f()) }
    private var mode = GL_MODELVIEW

    @Volatile
    var boundTexture: Int = 0

    var colorR: Float = 1f;
    var colorG: Float = 1f
    var colorB: Float = 1f;
    var colorA: Float = 1f

    @JvmStatic
    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        colorR = r; colorG = g; colorB = b; colorA = a
    }

    var overlayR: Float = 0f;
    var overlayG: Float = 0f
    var overlayB: Float = 0f;
    var overlayA: Float = 0f

    @JvmStatic
    fun setOverlay(r: Float, g: Float, b: Float, a: Float) {
        overlayR = r; overlayG = g; overlayB = b; overlayA = a
    }

    @JvmStatic
    fun clearOverlay() {
        overlayR = 0f; overlayG = 0f; overlayB = 0f; overlayA = 0f
    }

    @Volatile
    var lightS: Float = 0f
        private set

    @Volatile
    var lightT: Float = 0f
        private set

    @JvmStatic
    fun setLightCoords(s: Float, t: Float) {
        lightS = s; lightT = t
    }

    val boxUVData = IdentityHashMap<Any, IntArray>(256)

    fun setMode(m: Int) {
        mode = m
    }

    fun push() {
        val s = stack(); s.addLast(Matrix4f(s.last()))
    }

    fun pop() {
        val s = stack(); if (s.size > 1) s.removeLast()
    }

    fun loadIdentity() {
        top().identity()
    }

    fun translate(x: Float, y: Float, z: Float) {
        top().translate(x, y, z)
    }

    fun rotate(deg: Float, x: Float, y: Float, z: Float) {
        top().rotate(Math.toRadians(deg.toDouble()).toFloat(), x, y, z)
    }

    fun scale(x: Float, y: Float, z: Float) {
        top().scale(x, y, z)
    }

    fun mulMatrix(buf: FloatBuffer) {
        top().mul(Matrix4f().set(buf))
    }

    @JvmStatic
    fun getModelViewCopy(): Matrix4f = Matrix4f(modelStack.last())

    @JvmStatic
    fun getTextureCopy(): Matrix4f = Matrix4f(texStack.last())

    private fun stack(): ArrayDeque<Matrix4f> = when (mode) {
        GL_PROJECTION -> projStack
        GL_TEXTURE -> texStack
        else -> modelStack
    }

    private fun top(): Matrix4f = stack().last()
}