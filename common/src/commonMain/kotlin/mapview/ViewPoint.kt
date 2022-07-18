package mapview

/**
 * Observable position on the map. Includes observation coordinate and [zoom] factor
 */
data class ViewPoint(
    val focus: SchemeCoordinates,
    val scale: Float,
)

fun ViewPoint.move(x: Number, y: Number) = ViewPoint(
    focus = focus.copy(
        x = focus.x + x.toFloat(),
        y = focus.y + y.toFloat()
    ),
    scale = scale
)

fun ViewPoint.zoom(
    scaleDelta: Number,
    invariant: SchemeCoordinates = focus,
): ViewPoint {
    val newScale = scale.plus(scaleDelta.toFloat()).coerceIn(0f, Float.MAX_VALUE)
    return if (invariant == focus) {
        copy(scale = newScale)
    } else {
        ViewPoint(
            focus = focus,
            scale = newScale
        )
    }
}
