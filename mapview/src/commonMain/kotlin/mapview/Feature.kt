package mapview

import androidx.compose.ui.graphics.Brush
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

interface WithExtent {
    fun getExtent(): Extent
}

sealed interface FeatureType : WithExtent
interface PointFeatureType : FeatureType {
    val position: SchemeCoordinates
    override fun getExtent() = Extent(position, position)
}

interface RectFeatureType : FeatureType {
    val position: SchemeCoordinates
    val size: DpSize

    //TODO : fix on the AbstractView side
    override fun getExtent() = Extent(position, position)
}

interface ScaledRectFeatureType : FeatureType {
    val position: SchemeCoordinates
    val size: DpSize
    override fun getExtent(): Extent {
        //original size in dp is 1 to 1 as SchemeCoordinates
        return Extent(
            position,
            position + SchemeCoordinates(x = size.width.value.toDouble(), y = -size.height.value.toDouble())
        )
    }
}

interface LineFeatureType : FeatureType {
    val positionStart: SchemeCoordinates
    val positionEnd: SchemeCoordinates
    override fun getExtent() = listOf(positionStart, positionEnd).toExtent()
}

sealed class Feature : WithExtent {
    abstract val id: FeatureId
}


class CircleFeature(
    override val id: FeatureId,
    override val position: SchemeCoordinates,
    val radius: Dp,
    val color: Color,
    val style: DrawStyle = Fill,
) : Feature(), PointFeatureType

class LineFeature(
    override val id: FeatureId,
    override val positionStart: SchemeCoordinates,
    override val positionEnd: SchemeCoordinates,
    val color: Color,
    val width: Dp = Stroke.HairlineWidth.dp,
    val cap: StrokeCap = Stroke.DefaultCap,
) : Feature(), LineFeatureType

class ScaledRectFeature(
    override val id: FeatureId,
    //left top
    override val position: SchemeCoordinates,
    override val size: DpSize,
    val brush: Brush,
    val style: DrawStyle = Fill,
) : Feature(), ScaledRectFeatureType {

}

class TextFeature(
    override val id: FeatureId,
    override val position: SchemeCoordinates,
    val text: String,
    val color: Color,
) : Feature(), PointFeatureType

class ImageFeature(
    override val id: FeatureId,
    override val position: SchemeCoordinates,
    val painter: Painter,
    override val size: DpSize,
) : Feature(), RectFeatureType

class ScaledImageFeature(
    override val id: FeatureId,
    //left top
    override val position: SchemeCoordinates,
    val painter: Painter,
    override val size: DpSize,
) : Feature(), ScaledRectFeatureType
