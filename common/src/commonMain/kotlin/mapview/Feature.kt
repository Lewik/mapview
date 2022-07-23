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
import kotlin.jvm.JvmInline

@JvmInline
value class FeatureId(val value: String)
sealed class Feature {
    abstract fun getExtent(): Extent
    abstract val featureId: FeatureId
}

interface PointFeatureType {
    val position: SchemeCoordinates
}

interface LineFeatureType {
    val positionStart: SchemeCoordinates
    val positionEnd: SchemeCoordinates
}

class CircleFeature(
    override val featureId: FeatureId,
    override val position: SchemeCoordinates,
    val radius: Dp,
    val color: Color,
    val style: DrawStyle = Fill,
) : Feature(), PointFeatureType {

    override fun getExtent() = Extent(position, position)
}

class LineFeature(
    override val featureId: FeatureId,
    override val positionStart: SchemeCoordinates,
    override val positionEnd: SchemeCoordinates,
    val color: Color,
    val width: Dp = Stroke.HairlineWidth.dp,
) : Feature(), LineFeatureType {
    override fun getExtent() = listOf(positionStart, positionEnd).toExtent()
}

class TextFeature(
    override val featureId: FeatureId,
    override val position: SchemeCoordinates,
    val text: String,
    val color: Color,
) : Feature(), PointFeatureType {
    override fun getExtent() = Extent(position, position)
}

class BitmapImageFeature(
    override val featureId: FeatureId,
    val position: SchemeCoordinates,
    val image: ImageBitmap,
    val size: DpSize,
) : Feature() {
    override fun getExtent() = Extent(position, position)
}


class VectorImageFeature(
    override val featureId: FeatureId,
    val position: SchemeCoordinates,
    val painter: Painter,
    val size: DpSize,
) : Feature() {
    override fun getExtent() = Extent(position, position)
}
