package mapview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

sealed class Feature {
    abstract fun getExtent(): Extent
}

class CircleFeature(
    val position: SchemeCoordinates,
    val radius: Dp,
    val color: Color,
    val style: DrawStyle = Fill,
) : Feature() {

    override fun getExtent() = Extent(position, position)
}

class LineFeature(
    val positionStart: SchemeCoordinates,
    val positionEnd: SchemeCoordinates,
    val color: Color,
    val width: Dp = Stroke.HairlineWidth.dp,
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
    val size: DpSize,
) : Feature() {
    override fun getExtent() = Extent(position, position)
}


class VectorImageFeature(
    val position: SchemeCoordinates,
    val painter: Painter,
    val size: DpSize,
) : Feature() {
    override fun getExtent() = Extent(position, position)
}
