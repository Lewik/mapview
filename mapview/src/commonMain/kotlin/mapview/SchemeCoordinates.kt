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



