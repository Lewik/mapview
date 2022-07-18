@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package mapview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp


@Composable
fun SchemeView(
//    mapTileProvider: MapTileProvider,
//    computeViewPoint: (canvasSize: DpSize) -> MapViewPoint,
//    features: Map<FeatureId, MapFeature>,
//    onClick: MapViewPoint.() -> Unit,
//    config: MapViewConfig,
    features: List<Feature>,
    viewPoint: ViewPoint,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    modifier: Modifier,
) {

    var canvasSize by remember { mutableStateOf(DpSize(512.dp, 512.dp)) }

    val canvasModifier = modifier.pointerInput(Unit) {
        forEachGesture {
            awaitPointerEventScope {
                val event: PointerEvent = awaitPointerEvent()
                event.changes.forEach { change ->
                    if (event.buttons.isPrimaryPressed) {
                        //Evaluating selection frame
//                        val dragStart = change.position
//                        val dpPos = DpOffset(dragStart.x.toDp(), dragStart.y.toDp())
//                            onClick(MapViewPoint(dpPos.toGeodetic(), viewPoint.zoom))
                        drag(change.id) { dragChange ->
                            val dragAmount = dragChange.position - change.position
                            onViewPointChange(
                                viewPoint.move(
                                    x = -dragAmount.x.toDp().value / viewPoint.scale,
                                    y = -dragAmount.y.toDp().value / viewPoint.scale
                                )
                            )
                        }
                    }
                }
            }
        }
    }
        .pointerInput(Unit) {
            while (true) {
                val event = awaitPointerEventScope {
                    awaitPointerEvent()
                }
                val current = event.changes.first().position
                if (event.type == PointerEventType.Scroll) {
                    val scrollY = event.changes.first().scrollDelta.y
                    if (scrollY != 0f) {
                        val zoomSpeed = 1.0 / 3.0
                        onViewPointChange(viewPoint.zoom(-scrollY * zoomSpeed, SchemeCoordinates(current.x, current.y)))
                    }
                }
            }
        }
        .fillMaxSize()



    Canvas(canvasModifier) {
        fun SchemeCoordinates.toOffset(): Offset = Offset(
            (canvasSize.width / 2 + (x.dp - viewPoint.focus.x.dp) * viewPoint.scale).toPx(),
            (canvasSize.height / 2 + (y.dp - viewPoint.focus.y.dp) * viewPoint.scale).toPx()
        )

        fun DrawScope.drawFeature(feature: Feature) {
            when (feature) {
                is CircleFeature -> drawCircle(
                    color = feature.color,
                    radius = feature.radius,
                    center = feature.position.toOffset()
                )
                is LineFeature -> drawLine(
                    color = feature.color,
                    start = feature.positionStart.toOffset(),
                    end = feature.positionEnd.toOffset()
                )
                is BitmapImageFeature -> drawImage(
                    image = feature.image,
                    topLeft = feature.position.toOffset()
                )
                is VectorImageFeature -> {
                    val offset = feature.position.toOffset()
                    translate(
                        left = offset.x - feature.size.width / 2,
                        top = offset.y - feature.size.height / 2
                    ) {
                        with(feature.painter) {
                            draw(feature.size)
                        }
                    }
                }
                is TextFeature -> TODO()
//                is TextFeature -> drawIntoCanvas { canvas ->
//                    val offset = feature.position.toOffset()
//                    canvas.nativeCanvas.drawString(
//                        feature.text,
//                        offset.x + 5,
//                        offset.y - 5,
//                        Font().apply { size = 16f },
//                        feature.color.toPaint()
//                    )
//                }
//                is CustomFeature -> drawIntoCanvas { canvas ->
//                    val offset = feature.position.toOffset()
//                    feature.drawFeature(this, offset)
//                }
            }.exhaustive()
        }

        if (canvasSize != size.toDpSize()) {
            canvasSize = size.toDpSize()
//            println("Recalculate canvas. Size: $size")
        }
        clipRect {
//            val tileSize = IntSize(
//                ceil((mapTileProvider.tileSize.dp * tileScale.toFloat()).toPx()).toInt(),
//                ceil((mapTileProvider.tileSize.dp * tileScale.toFloat()).toPx()).toInt()
//            )
//            mapTiles.forEach { (id, image) ->
//                //converting back from tile index to screen offset
//                val offset = IntOffset(
//                    (canvasSize.width / 2 + (mapTileProvider.toCoordinate(id.i).dp - centerCoordinates.x.dp) * tileScale.toFloat()).roundToPx(),
//                    (canvasSize.height / 2 + (mapTileProvider.toCoordinate(id.j).dp - centerCoordinates.y.dp) * tileScale.toFloat()).roundToPx()
//                )
//                drawImage(
//                    image = image,
//                    dstOffset = offset,
//                    dstSize = tileSize
//                )
//            }
            features.forEach { feature ->
                drawFeature(feature)
            }
        }
    }
}
