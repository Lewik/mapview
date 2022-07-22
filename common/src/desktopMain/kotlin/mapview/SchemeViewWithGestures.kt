@file:OptIn(ExperimentalComposeUiApi::class)

package mapview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.positionChange

@Composable
actual fun SchemeViewWithGestures(
    mapTileProvider: MapTileProvider?,
    features: State<List<Feature>>,
    viewPoint: State<ViewPoint>,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    onResize: (size: Size) -> Unit,
    modifier: Modifier,
) {
    val canvasModifier = modifier
        .onPointerEvent(PointerEventType.Scroll) {
            val change = it.changes.first()
            val scrollY = change.scrollDelta.y
            onViewPointChange(
                viewPoint.value.addScale(
                    -scrollY,// * 1.0 / 3.0,
//                    SchemeCoordinates(change.position.x / viewPoint.value.scale, change.position.y / viewPoint.value.scale)
                )
            )
        }
        .onPointerEvent(PointerEventType.Move) {
            if (it.buttons.isPrimaryPressed) {
                val dragAmount = it.changes.first().positionChange()
                onViewPointChange(
                    viewPoint.value.move(
                        x = dragAmount.x,
                        y = dragAmount.y
                    )
                )
            }
        }


    SchemeView(
        mapTileProvider = mapTileProvider,
        features = features.value,
        viewPoint = viewPoint.value,
        onViewPointChange = onViewPointChange,
        onResize = onResize,
        modifier = canvasModifier,
    )
}
