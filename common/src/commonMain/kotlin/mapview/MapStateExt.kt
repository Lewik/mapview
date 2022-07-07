package mapview

import mapview.GlobalMercator.toMeters
import kotlin.math.max

//fun InternalMapState.geoLengthToDisplay(geoLength: Double): Int {
//    return (height * geoLength * zoom).toInt()
//}

//fun InternalMapState.geoXToDisplay(x: Double): Int = geoLengthToDisplay(x - topLeft.x)
//fun InternalMapState.geoYToDisplay(y: Double): Int = geoLengthToDisplay(y - topLeft.y)

//fun InternalMapState.geoToDisplay(geoPt: GeoPt): Pt = Pt(geoXToDisplay(geoPt.x), geoYToDisplay(geoPt.y))
//fun InternalMapState.displayLengthToGeo(displayLength: Int): Double = displayLength / (zoom * height)
//fun InternalMapState.displayLengthToGeo(pt: Pt): GeoPt = GeoPt(displayLengthToGeo(pt.x), displayLengthToGeo(pt.y))

//fun InternalMapState.displayToGeo(displayPt: Pt): GeoPt {
//    val x1 = displayLengthToGeo((displayPt.x))
//    val y1 = displayLengthToGeo((displayPt.y))
//    return topLeft + GeoPt(x1, y1)
//}

/**
 * Функция 2^x
 */
fun pow2(x: Int): Int {
    if (x < 0) {
        return 0
    }
    return 1 shl x
}

fun InternalMapState.zoom(
    zoomCenter: DisplayPixel?,
    change: Double,
): InternalMapState {
    @Suppress("NAME_SHADOWING")
    val zoomCenter = zoomCenter ?: getCenterOffset()

    val oldZoom = zoom
    val newZoom = max(0.0, oldZoom + change * 0.05)

    val (oldMetersCenterX, oldMetersCenterY) = zoomCenter
        .toMeters(oldZoom)

    val (newMetersCenterX, newMetersCenterY) = zoomCenter
        .toMeters(oldZoom)

    return copy(
        zoom = newZoom,
        // center = Meters(x = center.x + oldMetersCenterX - newMetersCenterX, y = center.y + oldMetersCenterY - newMetersCenterY)
    )
////    val geoDelta = state.displayToGeo(pt) - scaledState.displayToGeo(pt)
//    return scaledState.copy(
////        center =
//        topLeft = scaledState.topLeft + geoDelta,
//    ).correctGeoXY()
}
