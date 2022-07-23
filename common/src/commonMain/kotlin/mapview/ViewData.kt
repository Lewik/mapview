package mapview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Observable position on the map. Includes observation coordinate and [scale] factor
 */
data class ViewData(
    val size: Size,
    val focus: SchemeCoordinates,
    val scale: Double,
    val showDebug: Boolean,
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
fun ViewData.move(dragAmount: Offset) = copy(
    focus = SchemeCoordinates(
        x = focus.x - dragAmount.x / scale,
        y = focus.y + dragAmount.y / scale
    ),
    scale = scale,
    size = size
)

fun ViewData.addScale(
    scaleDelta: Number,
//    invariant: SchemeCoordinates = focus,
): ViewData {
    println("scaleDelta $scaleDelta")
    val newScale = (scale * (1 + scaleDelta.toFloat() / 10)).coerceIn(0.0, Double.MAX_VALUE)
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

