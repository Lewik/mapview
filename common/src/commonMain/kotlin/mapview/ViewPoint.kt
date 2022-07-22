package mapview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Observable position on the map. Includes observation coordinate and [scale] factor
 */
data class ViewPoint(
    val size: Size,
    val focus: SchemeCoordinates,
    val scale: Double,
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
}

//in display coordinates
fun ViewPoint.move(x: Number, y: Number) = ViewPoint(
    focus = SchemeCoordinates(
        x = focus.x - x.toFloat() / scale,
        y = focus.y + y.toFloat() / scale
    ),
    scale = scale,
    size = size
)

fun ViewPoint.addScale(
    scaleDelta: Number,
//    invariant: SchemeCoordinates = focus,
): ViewPoint {
    val newScale = scale
        .plus(scaleDelta.toFloat())
        .coerceIn(1.0, Double.MAX_VALUE)
    return copy(scale = newScale)
//    return if (invariant == focus) {
//        copy(scale = newScale)
//    } else {
//        ViewPoint(
//            focus = focus,
//            scale = newScale
//        )
//    }
}

