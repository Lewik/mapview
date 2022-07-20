@file:OptIn(ExperimentalComposeUiApi::class)

package mapview

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.positionChange

@Composable
actual fun SchemeViewWithGestures(
    features: List<Feature>,
    viewPoint: ViewPoint,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    modifier: Modifier,
) {
    val canvasModifier = modifier
        .onPointerEvent(PointerEventType.Scroll) {
            val change = it.changes.first()
            val scrollY = change.scrollDelta.y
            onViewPointChange(
                viewPoint.zoom(
                    -scrollY * 1.0 / 3.0,
                    SchemeCoordinates(change.position.x / viewPoint.scale, change.position.y / viewPoint.scale)
                )
            )
        }
        .onPointerEvent(PointerEventType.Move) {
            if (it.buttons.isPrimaryPressed) {
                val dragAmount = it.changes.first().positionChange()
                onViewPointChange(
                    viewPoint.move(
                        x = -dragAmount.x.toDp().value / viewPoint.scale,
                        y = -dragAmount.y.toDp().value / viewPoint.scale
                    )
                )
            }
        }


    SchemeView(
        features = features,
        viewPoint = viewPoint,
        onViewPointChange = onViewPointChange,
        modifier = canvasModifier
    )
}
