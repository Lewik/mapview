package mapview.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.*
import mapview.*
import mapview.viewData.ViewData

@Composable
internal fun AbstractView(
    features: List<Feature>,
    viewDataState: State<ViewData>,
    onDragStart: (offset: Offset) -> Unit = {},
    onDrag: (dragAmount: Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onScroll: (scrollY: Float, target: Offset?) -> Unit = { _, _ -> },
    onClick: (offset: Offset) -> Unit = {},
    onResize: (size: Size) -> Unit,
    modifier: Modifier = Modifier,
    tileSizeXY: IntSize,
    mapTiles: SnapshotStateList<MapTile>,
) {
    val viewData by derivedStateOf { viewDataState.value } //TODO is it correct?
    with(viewData) {

        val canvasModifier = modifier
            .canvasGestures(
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                onScroll = onScroll,
                onClick = onClick
            )
            .fillMaxSize()


        Canvas(canvasModifier) {
            if (viewData.size != size) {
                onResize(size)
            }
            clipRect {
                mapTiles.forEach { (tileId, image) ->
                    val offset = tileId
                        .toSchemaCoordinates()
                        .toOffset()
                    val intOffset = offset
                        .round()
//                        println("tile $tileId tileNum $tileNum int offset $offset, tileSizeXY $tileSizeXY")
                    drawImage(
                        image = image,
                        dstOffset = intOffset,
                        dstSize = tileSizeXY
                    )
                    if (viewData.showDebug) {
                        drawRect(
                            color = androidx.compose.ui.graphics.Color.Red,
                            topLeft = intOffset.toOffset(),
                            size = tileSizeXY.toSize(),
                            style = Stroke(width = 1f)
                        )
                        drawIntoCanvas {
                            it.nativeCanvas.drawText1(
                                string = "x: ${tileId.x}",
                                x = offset.x + 20.dp.toPx(),
                                y = offset.y + 20.dp.toPx(),
                                fontSize = 10.dp.toPx(),
                                paint = Paint().apply { color = androidx.compose.ui.graphics.Color.Red }
                            )
                            it.nativeCanvas.drawText1(
                                string = "y: ${tileId.y}",
                                x = offset.x + 20.dp.toPx(),
                                y = offset.y + 40.dp.toPx(),
                                fontSize = 10.dp.toPx(),
                                paint = Paint().apply { color = androidx.compose.ui.graphics.Color.Red }
                            )
                            it.nativeCanvas.drawText1(
                                string = "zoom: ${tileId.zoom}",
                                x = offset.x + 20.dp.toPx(),
                                y = offset.y + 60.dp.toPx(),
                                fontSize = 10.dp.toPx(),
                                paint = Paint().apply { color = androidx.compose.ui.graphics.Color.Red }
                            )
                        }
                    }
                }
                features.forEach { feature ->
                    when (feature) {
                        is CircleFeature -> drawCircle(
                            color = feature.color,
                            radius = feature.radius.toPx(),
                            center = feature.position.toOffset(),
                            style = feature.style,
                        )
                        is LineFeature -> drawLine(
                            color = feature.color,
                            start = feature.positionStart.toOffset(),
                            end = feature.positionEnd.toOffset(),
                            strokeWidth = feature.width.toPx(),
                            cap = feature.cap
                        )
                        is BitmapImageFeature -> drawImage(
                            image = feature.image,
                            topLeft = feature.position.toOffset(),
                        )
                        is VectorImageFeature -> {
                            val offset = feature.position.toOffset()
                            translate(
                                left = offset.x,
                                top = offset.y
                            ) {
                                with(feature.painter) {
                                    draw(size = feature.size.toSize())
                                }
                            }
                        }
                        is TextFeature -> drawIntoCanvas { canvas ->
                            val offset = feature.position.toOffset()
                            canvas.nativeCanvas.drawText1(
                                string = feature.text,
                                x = offset.x + 5,
                                y = offset.y - 5,
                                fontSize = 16f,
                                paint = Paint().apply { color = feature.color }
                            )
                        }
                    }.exhaustive()

                }
                if (viewData.showDebug) {
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.DarkGray,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height)
                    )
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.DarkGray,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2)
                    )
                }

            }
        }

        if (viewData.showDebug) {
            Box {
                Column(
                    modifier = androidx.compose.ui.Modifier
                        .background(color = androidx.compose.ui.graphics.Color.White.copy(alpha = .5f))
                        .padding(10.dp)
                ) {
                    Text("Focus: x: ${viewData.focus.x}, y: ${viewData.focus.x}")
                    Text("Scale: ${viewData.scale}, min: ${viewData.getMinScaleCoerce()}, max: ${viewData.maxScale}")
                    if (mapTiles.isNotEmpty()) {
                        val zoom = mapTiles.firstOrNull()?.id?.zoom
                        Text("Zoom: $zoom")
                    }
                    Text("Size: width:${viewData.size.width}, height: ${viewData.size.height}")
                }
            }
        }
    }
}
