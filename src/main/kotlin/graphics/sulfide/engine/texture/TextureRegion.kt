package graphics.sulfide.engine.texture

class TextureRegion(
    val layer: Int,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val atlasSize: Int
) {
    val u0: Float get() = x.toFloat() / atlasSize
    val v0: Float get() = y.toFloat() / atlasSize

    val uScale: Float get() = width.toFloat() / atlasSize
    val vScale: Float get() = height.toFloat() / atlasSize
}

