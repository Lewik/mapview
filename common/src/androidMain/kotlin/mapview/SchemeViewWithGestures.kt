package mapview

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput

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
        .pointerInput(Unit) {
            detectTransformGestures(
                onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                    onViewPointChange(
                        viewPoint
                            .value
                            .move(-pan)
                            .addScale(
                                -gestureZoom * 1.0 / 3.0
//                            SchemeCoordinates(change.position.x / viewPoint.scale, change.position.y / viewPoint.scale)
                            )
                    )
                }
            )
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



