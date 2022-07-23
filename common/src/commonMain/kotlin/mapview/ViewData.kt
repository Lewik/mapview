package mapview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.max
import kotlin.math.min

/**
 * Observable position on the map. Includes observation coordinate and [scale] factor
 */
data class ViewData(
    val size: Size,
    val focus: SchemeCoordinates,
    val scale: Double,
    val showDebug: Boolean,
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


    //in display coordinates
    fun move(dragAmount: Offset) = copy(
        focus = SchemeCoordinates(
            x = focus.x - dragAmount.x / scale,
            y = focus.y + dragAmount.y / scale
        ),
        scale = scale.coerce(),
        size = size
    )

    fun addScale(
        scaleDelta: Number,
//    invariant: SchemeCoordinates = focus,
    ): ViewData {
        println("scaleDelta $scaleDelta")
        val newScale = (scale * (1 + scaleDelta.toFloat() / 10)).coerce()
        return copy(scale = newScale)
//    return if (invariant == focus) {
//        copy(scale = newScale)
//    } else {
//        ViewData(
//            focus = focus,
//            scale = newScale
//        )
//    }
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

