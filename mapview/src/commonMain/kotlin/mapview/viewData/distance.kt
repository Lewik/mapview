package mapview.viewData

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import mapview.*
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * returns sorted by distance
 */
fun <T> getClosestFeaturesIds(
    density: Density,
    viewData: ViewData,
    features: List<T>,
    offset: Offset,
    hitTolerance: Dp,
//): List<FeatureId> where T : Feature, T : FeatureType {
): List<FeatureId> where T : Feature {
    val hitToleranceSquared = with(density) { hitTolerance.toPx().pow(2) }
    return features.map { it.id to viewData.getSquaredDistance(offset, it as FeatureType) }
        .filter { it.second < hitToleranceSquared }
        .sortedBy { it.second }
        .map { it.first }
}

fun ViewData.getPixelDistance(target: Offset, feature: FeatureType, density: Density) =
    with(density) {
        sqrt(getSquaredDistance(target, feature)).toDp()
    }

private fun ViewData.getSquaredDistance(target: Offset, feature: FeatureType) = when (feature) {
    is PointFeatureType -> getSquaredDistanceToPoint(target, feature)
    is RectFeatureType -> getSquaredDistanceToRect(target, feature)
    is ScaledRectFeatureType -> getSquaredDistanceToScaledRect(target, feature)
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

private fun ViewData.getSquaredDistanceToRect(target: Offset, feature: RectFeatureType): Float {
    //TODO make as rect not point
    val offset = feature.position.toOffset()
    return (target.x - offset.x).pow(2) + (target.y - offset.y).pow(2)
}

private fun ViewData.getSquaredDistanceToScaledRect(target: Offset, feature: ScaledRectFeatureType): Float {
    //TODO make as SCALED rect not point
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
