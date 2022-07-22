package mapview

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.State
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.canvasGestures(
    viewPoint: State<ViewPoint>,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    onClick: (point: SchemeCoordinates) -> Unit,
): Modifier {
    return Modifier
        .onPointerEvent(PointerEventType.Scroll) {
            val change = it.changes.first()
            val scrollY = change.scrollDelta.y
//            val x1 = change.position.x - viewPoint.value.size.width / 2 * viewPoint.value.scale
//            val y2 = change.position.y - viewPoint.value.size.height / 2 * viewPoint.value.scale
//            println("position ${change.position.x} width ${viewPoint.value.size.width} delta ${(change.position.x - viewPoint.value.size.width / 2) * (scrollY - 1)}")
            onViewPointChange(
                viewPoint.value
                    .copy(
//                        focus = SchemeCoordinates(
//                            viewPoint.value.focus.x + (change.position.x/ viewPoint.value.scale - viewPoint.value.focus.x) * scrollY,
//                            viewPoint.value.focus.y + (change.position.y/ viewPoint.value.scale - viewPoint.value.focus.y) * scrollY,
//                            ),
                        scale = viewPoint.value.scale
                            .plus(-scrollY)
                            .coerceIn(.0, Double.MAX_VALUE)
                    )
//                    .move(
//                        viewPoint.value.focus.x + (change.position.x/ viewPoint.value.scale - viewPoint.value.focus.x) * viewPoint.value.scale,
//                        viewPoint.value.focus.y + (change.position.y/ viewPoint.value.scale - viewPoint.value.focus.y) * viewPoint.value.scale,
//                    )
//                    .zoom(-scrollY)
            )
        }
        .onPointerEvent(PointerEventType.Move) {
            if (it.buttons.isPrimaryPressed) {
                val dragAmount = it.changes.first().positionChange()
                onViewPointChange(
                    viewPoint.value.move(
                        x = dragAmount.x,
                        y = dragAmount.y
                    )
                )
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    onClick(
                        with(viewPoint.value) {
                            it.toSchemeCoordinates()
                        }
                    )
                },
            )
        }

}
