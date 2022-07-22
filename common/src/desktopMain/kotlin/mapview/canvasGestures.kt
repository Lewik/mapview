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
            onViewPointChange(
                viewPoint.value
                    .addScale(-scrollY)
            )
        }
        .onPointerEvent(PointerEventType.Move) {
            if (it.buttons.isPrimaryPressed) {
                val dragAmount = it.changes.first().positionChange()
                onViewPointChange(
                    viewPoint.value.move(dragAmount)
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
