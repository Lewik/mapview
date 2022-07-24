@file:OptIn(ExperimentalFoundationApi::class)

package mapview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.canvasGestures(
    onDragStart: (offset: Offset) -> Unit,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onScroll: (scrollY: Float) -> Unit,
    onClick: (offset: Offset) -> Unit,
): Modifier {
    return Modifier
        .onPointerEvent(PointerEventType.Scroll) {
            onScroll(-it.changes.first().scrollDelta.y)
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = onDragStart,
                onDrag = { _, dragAmount -> onDrag(dragAmount) },
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
            )
        }
        .pointerInput(Unit) { detectTapGestures(onTap = onClick) }

}
