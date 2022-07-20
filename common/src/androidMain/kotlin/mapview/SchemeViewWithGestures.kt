package mapview

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
@Composable
actual fun SchemeViewWithGestures(
    mapTileProvider: MapTileProvider?,
    features: List<Feature>,
    viewPoint: ViewPoint,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    modifier: Modifier,
) {

    val canvasModifier = modifier
        .pointerInput(Unit) {
            detectTransformGestures(
                onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                    onViewPointChange(
                        viewPoint
                            .move(
                                x = -pan.x.toDp().value / viewPoint.scale,
                                y = -pan.y.toDp().value / viewPoint.scale
                            )
                            .zoom(
                                -gestureZoom * 1.0 / 3.0
//                            SchemeCoordinates(change.position.x / viewPoint.scale, change.position.y / viewPoint.scale)
                            )
                    )
                }
            )
        }


    SchemeView(
        mapTileProvider = mapTileProvider,
        features = features,
        viewPoint = viewPoint,
        onViewPointChange = onViewPointChange,
        modifier = canvasModifier
    )
}



