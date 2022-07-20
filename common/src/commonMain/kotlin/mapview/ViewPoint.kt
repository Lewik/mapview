package mapview

/**
 * Observable position on the map. Includes observation coordinate and [zoom] factor
 */
data class ViewPoint(
    val focus: SchemeCoordinates,
    val scale: Double,
)

fun ViewPoint.move(x: Number, y: Number) = ViewPoint(
    focus = SchemeCoordinates(
        x = focus.x + x.toFloat(),
        y = focus.y + y.toFloat()
    ),
    scale = scale
)

fun ViewPoint.zoom(
    scaleDelta: Number,
    invariant: SchemeCoordinates = focus,
): ViewPoint {
    val newScale = scale.plus(scaleDelta.toFloat()).coerceIn(.0, Double.MAX_VALUE)
    return if (invariant == focus) {
        copy(scale = newScale)
    } else {
        ViewPoint(
            focus = focus,
            scale = newScale
        )
    }
}
