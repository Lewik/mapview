package mapview

import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

expect fun Modifier.canvasGestures(
    viewData: State<ViewData>,
    onViewDataChange: (viewData: ViewData) -> Unit,
    onClick: (offset: Offset) -> Unit,
): Modifier
