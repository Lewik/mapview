package mapview

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize

sealed class Feature {
    abstract fun getExtent(): Extent
}

class CircleFeature(
    val position: SchemeCoordinates,
    val radius: Float,
    val color: Color,
) : Feature() {
    override fun getExtent() = Extent(position, position)
}

class LineFeature(
    val positionStart: SchemeCoordinates,
    val positionEnd: SchemeCoordinates,
    val color: Color,
) : Feature() {
    override fun getExtent() = listOf(positionStart, positionEnd).toExtent()
}

class TextFeature(
    val position: SchemeCoordinates,
    val text: String,
    val color: Color,
) : Feature() {
    override fun getExtent() = Extent(position, position)
}

class BitmapImageFeature(
    val position: SchemeCoordinates,
    val image: ImageBitmap,
    val size: IntSize,
) : Feature() {
    override fun getExtent() = Extent(position, position)
}


class VectorImageFeature(
    val position: SchemeCoordinates,
    val painter: Painter,
    val size: Size,
) : Feature() {
    override fun getExtent() = Extent(position, position)
}
