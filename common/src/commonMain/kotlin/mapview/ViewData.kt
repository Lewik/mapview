package mapview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

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


fun ViewData.getSquaredDistance(target: Offset, feature: FeatureType) = when (feature) {
    is PointFeatureType -> getSquaredDistanceToPoint(target, feature)
    is LineFeatureType -> getSquaredDistanceToLine(target, feature)
}

private fun ViewData.getSquaredDistanceToLine(target: Offset, feature: LineFeatureType): Float {
    val offsetStart = feature.positionStart.toOffset()
    val offsetEnd = feature.positionEnd.toOffset()
    return getSquaredDistance(
        x = target.x,
        y = target.y,
        x1 = offsetStart.x,
        y1 = offsetStart.y,
        x2 = offsetEnd.x,
        y2 = offsetEnd.y
    )
}

private fun ViewData.getSquaredDistanceToPoint(target: Offset, feature: PointFeatureType): Float {
    val offset = feature.position.toOffset()
    return (target.x - offset.x).pow(2) + (target.y - offset.y).pow(2)
}


//https://stackoverflow.com/questions/30559799/function-for-finding-the-distance-between-a-point-and-an-edge-in-java
private fun getSquaredDistance(x: Float, y: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {

    val a = x - x1
    val b = y - y1
    val c = x2 - x1
    val d = y2 - y1

    val lenSq = c * c + d * d
    val param = if (lenSq != 0f) { //in case of 0 length line
        val dot = a * c + b * d
        dot / lenSq
    } else {
        -1.0f
    }

    val (xx, yy) = when {
        param < 0f -> x1 to y1
        param > 1f -> x2 to y2
        else -> x1 + param * c to y1 + param * d
    }

    val dx = x - xx
    val dy = y - yy
    return dx * dx + dy * dy
}
