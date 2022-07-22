package mapview

import androidx.compose.runtime.State
import androidx.compose.ui.Modifier

expect fun Modifier.canvasGestures(
    viewPoint: State<ViewPoint>,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    onClick: (point: SchemeCoordinates) -> Unit
): Modifier
