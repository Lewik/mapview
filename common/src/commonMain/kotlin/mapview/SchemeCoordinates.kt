package mapview

import kotlin.math.pow

data class SchemeCoordinates(
    val x: Double,
    val y: Double,
) {
    operator fun plus(schemeCoordinates: SchemeCoordinates) = copy(
        x = x + schemeCoordinates.x,
        y = y + schemeCoordinates.y
    )

    operator fun minus(schemeCoordinates: SchemeCoordinates) = copy(
        x = x - schemeCoordinates.x,
        y = y - schemeCoordinates.y
    )

    operator fun times(a: Number) = copy(
        x = x * a.toDouble(),
        y = y * a.toDouble()
    )

    operator fun div(a: Number) = copy(
        x = x / a.toDouble(),
        y = y / a.toDouble()
    )
}




//https://stackoverflow.com/questions/30559799/function-for-finding-the-distance-between-a-point-and-an-edge-in-java
fun getSquaredDistance(x: Double, y: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double {

    val a = x - x1
    val b = y - y1
    val c = x2 - x1
    val d = y2 - y1

    val lenSq = c * c + d * d
    val param = if (lenSq != .0) { //in case of 0 length line
        val dot = a * c + b * d
        dot / lenSq
    } else {
        -1.0
    }

    val (xx, yy) = when {
        param < 0 -> x1 to y1
        param > 1 -> x2 to y2
        else -> x1 + param * c to y1 + param * d
    }

    val dx = x - xx
    val dy = y - yy
    return dx * dx + dy * dy
}

fun getSquaredDistance(target: SchemeCoordinates, line: LineFeatureType) = getSquaredDistance(
    x = target.x,
    y = target.y,
    x1 = line.positionStart.x,
    y1 = line.positionStart.y,
    x2 = line.positionEnd.x,
    y2 = line.positionEnd.y
)

fun getSquaredDistance(target: SchemeCoordinates, point: PointFeatureType) =
    (target.x - point.position.x).pow(2) + (target.y - point.position.y).pow(2)


