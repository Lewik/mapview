package mapview

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

actual fun Modifier.canvasGestures(
    onDragStart: (offset: Offset) -> Unit,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onScroll: (scrollY: Float, target: Offset?) -> Unit,
    onClick: (offset: Offset) -> Unit,
    features: List<Feature>,
) = pointerInput(features) { detectTapGestures(onTap = onClick) }
    .pointerInput(features) {
        detectDragGestures(
            onDragStart = onDragStart,
            onDrag = { _, dragAmount -> onDrag(dragAmount) },
            onDragEnd = onDragEnd,
            onDragCancel = onDragCancel,
        )
    }
    .composed {
        val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
            onScroll(zoomChange, null)
//                rotation += rotationChange
            onDrag(offsetChange)
        }
        transformable(state = state)
    }
