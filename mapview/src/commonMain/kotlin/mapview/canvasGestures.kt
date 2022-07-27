package mapview

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

expect fun Modifier.canvasGestures(
    onDragStart: (offset: Offset) -> Unit,
    onDrag: (dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onScroll: (scrollY: Float, target: Offset?) -> Unit,
    onClick: (offset: Offset) -> Unit,
): Modifier
