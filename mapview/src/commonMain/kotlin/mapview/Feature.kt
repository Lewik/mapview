package mapview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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

sealed interface FeatureType
interface PointFeatureType : FeatureType {
    val position: SchemeCoordinates
}

interface LineFeatureType : FeatureType {
    val positionStart: SchemeCoordinates
    val positionEnd: SchemeCoordinates
}

sealed class Feature {
    abstract fun getExtent(): Extent
    abstract val id: FeatureId
}


class CircleFeature(
    override val id: FeatureId,
    override val position: SchemeCoordinates,
    val radius: Dp,
    val color: Color,
    val style: DrawStyle = Fill,
) : Feature(), PointFeatureType {

    override fun getExtent() = Extent(position, position)
}

class LineFeature(
    override val id: FeatureId,
    override val positionStart: SchemeCoordinates,
    override val positionEnd: SchemeCoordinates,
    val color: Color,
    val width: Dp = Stroke.HairlineWidth.dp,
    val cap: StrokeCap = Stroke.DefaultCap,
) : Feature(), LineFeatureType {

    override fun getExtent() = listOf(positionStart, positionEnd).toExtent()
}

class TextFeature(
    override val id: FeatureId,
    override val position: SchemeCoordinates,
    val text: String,
    val color: Color,
) : Feature(), PointFeatureType {
    override fun getExtent() = Extent(position, position)
}

class ImageFeature(
    override val id: FeatureId,
    val position: SchemeCoordinates,
    val painter: Painter,
    val size: DpSize,
) : Feature() {
    override fun getExtent() = Extent(position, position)
}

class ScaledImageFeature(
    override val id: FeatureId,
    //left top
    val position: SchemeCoordinates,
    val painter: Painter,
    val size: DpSize,
) : Feature() {
    override fun getExtent(): Extent {
        //original size in dp is 1 to 1 as SchemeCoordinates
        return Extent(
            position,
            position + SchemeCoordinates(x = size.width.value.toDouble(), y = -size.height.value.toDouble())
        )
    }
}
