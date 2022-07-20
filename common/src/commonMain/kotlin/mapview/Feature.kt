package mapview

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize

data class SchemeCoordinates(
    val x: Double,
    val y: Double,
) {
    operator fun plus(schemeCoordinates: SchemeCoordinates) = copy(
        x = x + schemeCoordinates.x,
        y = y + schemeCoordinates.y
    )

    operator fun minus(schemeCoordinates: SchemeCoordinates) = copy(
        x = x - schemeCoordinates.x,
        y = y - schemeCoordinates.y
    )

    operator fun times(a: Number) = copy(
        x = x * a.toFloat(),
        y = y * a.toFloat()
    )
}

sealed class Feature {
    abstract fun getBoundingBox(): BoundingBox
}

class CircleFeature(
    val position: SchemeCoordinates,
    val radius: Float,
    val color: Color,
) : Feature() {
    override fun getBoundingBox() = BoundingBox(position, position)
}

class LineFeature(
    val positionStart: SchemeCoordinates,
    val positionEnd: SchemeCoordinates,
    val color: Color,
) : Feature() {
    override fun getBoundingBox() = listOf(positionStart, positionEnd).toBoundingBox()
}

class TextFeature(
    val position: SchemeCoordinates,
    val text: String,
    val color: Color,
) : Feature() {
    override fun getBoundingBox() = BoundingBox(position, position)
}

class BitmapImageFeature(
    val position: SchemeCoordinates,
    val image: ImageBitmap,
    val size: IntSize,
) : Feature() {
    override fun getBoundingBox() = BoundingBox(position, position)
}


class VectorImageFeature(
    val position: SchemeCoordinates,
    val painter: Painter,
    val size: Size,
) : Feature() {
    override fun getBoundingBox() = BoundingBox(position, position)
}
