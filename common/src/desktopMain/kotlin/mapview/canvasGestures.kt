package mapview

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.State
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.canvasGestures(
    viewData: State<ViewData>,
    onViewDataChange: (viewData: ViewData) -> Unit,
    onClick: (point: SchemeCoordinates) -> Unit,
): Modifier {
    return Modifier
        .onPointerEvent(PointerEventType.Scroll) {
            val change = it.changes.first()
            val scrollY = change.scrollDelta.y
            onViewDataChange(
                viewData.value
                    .addScale(-scrollY)
            )
        }
        .onPointerEvent(PointerEventType.Move) {
            if (it.buttons.isPrimaryPressed) {
                val dragAmount = it.changes.first().positionChange()
                onViewDataChange(
                    viewData.value.move(dragAmount)
                )
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    onClick(
                        with(viewData.value) {
                            it.toSchemeCoordinates()
                        }
                    )
                },
            )
        }

}
