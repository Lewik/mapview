package mapview.viewData

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import mapview.Extent
import mapview.Feature
import mapview.SchemeCoordinates
import mapview.tiles.MapTileProvider
import mapview.toExtent
import kotlin.math.max
import kotlin.math.min

/**
 * Observable position on the map. Includes observation coordinate and [scale] factor
 */
data class ViewData(
    val size: Size,
    val focus: SchemeCoordinates = SchemeCoordinates(.0, .0),
    val scale: Double = 1.0,
    val showDebug: Boolean = false,
    val minScale: Double? = null,
    val maxScale: Double? = Double.MAX_VALUE,
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

    fun zoomToExtent(extent: Extent): ViewData {
        val extentWidth = extent.b.x - extent.a.x
        val extentHeight = extent.b.y - extent.a.y
        val widthScale = size.width / extentWidth
        val heightScale = size.height / extentHeight
        val newScale = min(widthScale, heightScale)
        return copy(
            focus = extent.center,
            scale = newScale.coerce()
        )
    }

    fun zoomToFeatures(features: Iterable<Feature>) = zoomToExtent(features.toExtent())
    fun getMinScaleCoerce() = .0.coerce()
    private fun Double.coerce(): Double {
        val absoluteMin = min(size.height.toDouble(), size.width.toDouble()) / MapTileProvider.EQUATOR
        val min = max(minScale ?: .0, absoluteMin)

        return coerceIn(min, maxScale)
    }
}


