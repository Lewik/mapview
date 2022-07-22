package mapview

import androidx.compose.ui.Modifier
import androidx.compose.runtime.State

actual fun Modifier.canvasGestures(viewPoint: State<ViewPoint>, onViewPointChange: (viewPoint: ViewPoint) -> Unit, onClick: (point: SchemeCoordinates) -> Unit): Modifier {
    TODO("Not yet implemented")
}
