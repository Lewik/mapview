package mapview

import mapview.GlobalMercator.toLatLon

fun InternalMapState.toShortString(): String = buildString {
    appendLine("width: $width, height: $height")
    appendLine("zoom: ${zoom.toShortString()}")
//    appendLine("center mercator: $center")
//    appendLine("center lat lon: ${center.toLatLon()}")
}

fun Double.toShortString(significantDigits: Int = 5): String {
    var multiplier: Long = 1
    repeat(significantDigits) {
        multiplier *= 10
    }
    val result = (this * multiplier).toLong().toDouble() / multiplier
    return result.toString()
}
