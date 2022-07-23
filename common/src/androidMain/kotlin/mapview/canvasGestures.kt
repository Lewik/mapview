package mapview

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput

actual fun Modifier.canvasGestures(
    viewData: State<ViewData>,
    onViewDataChange: (viewData: ViewData) -> Unit,
    onClick: (point: SchemeCoordinates) -> Unit,
) =
    pointerInput(Unit) {
        detectTransformGestures(
            onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                onViewDataChange(
                    viewData
                        .value
                        .addScale(
                            gestureZoom * 1.0 / 3.0
//                            SchemeCoordinates(change.position.x / viewPoint.scale, change.position.y / viewPoint.scale)
                        )
                )
            }
        )
        detectDragGestures { change, dragAmount ->
            change.consumeAllChanges()
            onViewDataChange(
                viewData
                    .value
                    .move(dragAmount)
            )
        }
    }
