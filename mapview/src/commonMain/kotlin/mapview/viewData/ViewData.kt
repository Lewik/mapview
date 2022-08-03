package mapview.viewData

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mapview.Extent
import mapview.Feature
import mapview.SchemeCoordinates
import mapview.tiles.EQUATOR
import mapview.toExtent
import kotlin.math.max
import kotlin.math.min

/**
 * Observable position on the map. Includes observation coordinate and [scale] factor
 */
data class ViewData(
    val size: Size = Size(1f, 1f),
    val focus: SchemeCoordinates = SchemeCoordinates(.0, .0),
    val scale: Double = 1.0,
    val showDebug: Boolean = false,
    val minScale: Double? = null,
    val maxScale: Double? = Double.MAX_VALUE,
    val density: Density,
) {


    fun Offset.toSchemeCoordinates(
    ) = SchemeCoordinates(
        x = (x - size.width / 2) / scale + focus.x,
        y = -(y - size.height / 2) / scale + focus.y
    )

    fun SchemeCoordinates.toOffset(): Offset = Offset(
        ((x - focus.x) * scale).toFloat() + size.width / 2,
        -((y - focus.y) * scale).toFloat() + size.height / 2,
    )


    fun move(dragAmount: Offset) = copy(
        focus = SchemeCoordinates(
            x = focus.x - dragAmount.x / scale,
            y = focus.y + dragAmount.y / scale
        ),
        scale = scale.coerce(),
        size = size
    )

    fun resize(newSize: Size) = copy(size = newSize)

    fun addScale(
        scaleDelta: Float,
        target: Offset? = null,
    ): ViewData {
        val targetOffset = target ?: focus.toOffset()
        val focusOffset = focus.toOffset()
        val newScale = (scale * (1 + scaleDelta / 10)).coerce()
        return copy(
            focus = targetOffset.toSchemeCoordinates(),
            scale = newScale,
        )
            .move(targetOffset - focusOffset)
    }

    fun multiplyScale(
        multiplier: Float,
    ): ViewData {
        val newScale = (scale * multiplier).coerce()
        return copy(scale = newScale)
    }

    fun zoomToExtent(
        extent: Extent,
        padding: Dp = PaddingDefault,
    ): ViewData {
        val newScale = calculateScaleForExtent(extent, padding)
        return copy(
            focus = extent.center,
            scale = newScale
        )
    }

    fun calculateScaleForExtent(extent: Extent, padding: Dp = PaddingDefault): Double {
        val pxPadding = with(density) { padding.toPx() }.toDouble()
        val extentWidth = extent.b.x - extent.a.x
        val extentHeight = extent.b.y - extent.a.y
        val widthScale = (size.width - pxPadding) / extentWidth
        val heightScale = (size.height - pxPadding) / extentHeight
        return min(widthScale, heightScale).coerce()
    }

    fun zoomToFeatures(
        features: Iterable<Feature>,
        padding: Dp = PaddingDefault,
    ) = zoomToExtent(
        extent = features.toExtent(),
        padding = padding
    )

    fun getMinScaleCoerce() = .0.coerce()
    private fun Double.coerce(): Double {
        val absoluteMin = min(size.height.toDouble(), size.width.toDouble()) / EQUATOR
        val min = max(minScale ?: .0, absoluteMin)

        return coerceIn(min, maxScale)
    }

    companion object {
        val PaddingDefault = 100.dp
    }
}


