package mapview.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import mapview.tiles.MapTile
import mapview.tiles.toSchemaCoordinates
import mapview.viewData.ViewData

@Composable
internal fun AbstractView(
    features: State<List<Feature>>,
    viewData: State<ViewData>,
    onDragStart: (offset: Offset) -> Unit = {},
    onDrag: (dragAmount: Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onScroll: (scrollY: Float, target: Offset?) -> Unit = { _, _ -> },
    onClick: (offset: Offset) -> Unit = {},
    onFirstResize: (size: Size) -> Unit,
    onResize: (size: Size) -> Unit,
    //size should be specified: see Canvas
    modifier: Modifier,
    tileSizeXY: IntSize,
    mapTiles: SnapshotStateList<MapTile>,
) {
    with(viewData.value) {
        val canvasModifier = Modifier
            .canvasGestures(
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                onScroll = onScroll,
                onClick = onClick
            ).then(modifier)

        Box(canvasModifier) {
            var wasFirstResize by remember { mutableStateOf(false) }
            Canvas(Modifier.matchParentSize()) {
                fun DpOffset.toOffset() = Offset(x.toPx(), y.toPx())
                if (viewData.value.size != size) {
                    if (wasFirstResize) {
                        onResize(size)
                    } else {
                        wasFirstResize = true
                        onFirstResize(size)
                    }
                }
                clipRect {
                    mapTiles.forEach { mapTile ->
                        val offset = mapTile.id
                            .toSchemaCoordinates()
                            .toOffset()
                        val intOffset = offset
                            .round()
//                        println("tile $tileId tileNum $tileNum int offset $offset, tileSizeXY $tileSizeXY")
                        drawImage(
                            image = mapTile.image,
                            srcOffset = IntOffset(mapTile.offsetX, mapTile.offsetY),
                            srcSize = IntSize(mapTile.cropSize, mapTile.cropSize),
                            dstOffset = intOffset,
                            dstSize = tileSizeXY
                        )
                        if (viewData.value.showDebug) {
                            drawRect(
                                color = androidx.compose.ui.graphics.Color.Red,
                                topLeft = intOffset.toOffset(),
                                size = tileSizeXY.toSize(),
                                style = Stroke(width = 1f)
                            )
                            drawIntoCanvas {
                                it.nativeCanvas.drawText1(
                                    string = "x: ${mapTile.id.x}",
                                    x = offset.x + 20.dp.toPx(),
                                    y = offset.y + 20.dp.toPx(),
                                    fontSize = 10.dp.toPx(),
                                    paint = Paint().apply { color = androidx.compose.ui.graphics.Color.Red }
                                )
                                it.nativeCanvas.drawText1(
                                    string = "y: ${mapTile.id.y}",
                                    x = offset.x + 20.dp.toPx(),
                                    y = offset.y + 40.dp.toPx(),
                                    fontSize = 10.dp.toPx(),
                                    paint = Paint().apply { color = androidx.compose.ui.graphics.Color.Red }
                                )
                                it.nativeCanvas.drawText1(
                                    string = "zoom: ${mapTile.id.zoom}",
                                    x = offset.x + 20.dp.toPx(),
                                    y = offset.y + 60.dp.toPx(),
                                    fontSize = 10.dp.toPx(),
                                    paint = Paint().apply { color = androidx.compose.ui.graphics.Color.Red }
                                )
                            }
                        }
                    }
                    features.value.forEach { feature ->
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

                            is ImageFeature -> {
                                val offset = feature.position.toOffset() - feature.centerOffset.toOffset()
                                translate(
                                    left = offset.x,
                                    top = offset.y
                                ) {
                                    with(feature.painter) {
                                        draw(
                                            size = feature.size.toSize(),
                                            alpha = feature.alpha,
                                            colorFilter = feature.colorFilter,
                                        )
                                    }
                                }
                            }

                            is ScaledRectFeature -> {
                                val size = feature.size.toSizeAsValue() * scale.toFloat()
                                val offset = feature.position.toOffset()
                                drawRect(
                                    brush = feature.brush,
                                    topLeft = offset,
                                    size = size,
                                    style = feature.style
                                )
                            }

                            is ScaledImageFeature -> {
                                val size = feature.size.toSizeAsValue() * scale.toFloat()
                                val offset = feature.position.toOffset()
                                translate(
                                    left = offset.x,
                                    top = offset.y
                                ) {
                                    with(feature.painter) {
                                        draw(size = size)
                                    }
                                }
                            }

                            is TextFeature -> drawIntoCanvas { canvas ->
                                val offset = feature.position.toOffset()
                                canvas.nativeCanvas.drawText1(
                                    string = feature.text,
                                    x = offset.x + 5,
                                    y = offset.y - 5,
                                    fontSize = feature.fontSize.toPx(),
                                    paint = Paint().apply { color = feature.color }
                                )
                            }
                        }.exhaustive()

                    }
                    if (viewData.value.showDebug) {
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

            if (viewData.value.showDebug) {

                Column(
                    modifier = androidx.compose.ui.Modifier
                        .background(color = androidx.compose.ui.graphics.Color.White.copy(alpha = .5f))
                        .padding(10.dp)
                ) {
                    Text("Focus: x: ${viewData.value.focus.x}, y: ${viewData.value.focus.x}")
                    Text("Scale: ${viewData.value.scale}, min: ${viewData.value.getMinScaleCoerce()}, max: ${viewData.value.maxScale}")
                    if (mapTiles.isNotEmpty()) {
                        val zoom = mapTiles.firstOrNull()?.id?.zoom
                        Text("Zoom: $zoom")
                    }
                    Text("Size: width:${viewData.value.size.width}, height: ${viewData.value.size.height}")
                }
            }
        }
    }
}

private fun DpSize.toSizeAsValue() = Size(width.value, height.value)
