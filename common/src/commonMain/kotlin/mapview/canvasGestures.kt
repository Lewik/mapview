package mapview

import androidx.compose.runtime.State
import androidx.compose.ui.Modifier

expect fun Modifier.canvasGestures(
    viewData: State<ViewData>,
    onViewDataChange: (viewData: ViewData) -> Unit,
    onClick: (point: SchemeCoordinates) -> Unit,
): Modifier
