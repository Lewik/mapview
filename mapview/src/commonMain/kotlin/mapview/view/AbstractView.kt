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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.*
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
    asyncText: Boolean = true,
) {
    with(viewData.value) {
        val canvasModifier = Modifier
            .canvasGestures(
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                onScroll = onScroll,
                onClick = onClick,
                features = features.value
            ).then(modifier)

        Box(canvasModifier) {
            var wasFirstResize by remember { mutableStateOf(false) }
            Canvas(
                Modifier
                    .matchParentSize()
            ) {
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
                                color = Color.Red,
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
                                    paint = Paint().apply { color = Color.Red }
                                )
                                it.nativeCanvas.drawText1(
                                    string = "y: ${mapTile.id.y}",
                                    x = offset.x + 20.dp.toPx(),
                                    y = offset.y + 40.dp.toPx(),
                                    fontSize = 10.dp.toPx(),
                                    paint = Paint().apply { color = Color.Red }
                                )
                                it.nativeCanvas.drawText1(
                                    string = "zoom: ${mapTile.id.zoom}",
                                    x = offset.x + 20.dp.toPx(),
                                    y = offset.y + 60.dp.toPx(),
                                    fontSize = 10.dp.toPx(),
                                    paint = Paint().apply { color = Color.Red }
                                )
                            }
                        }
                    }

                    features.value.forEach { feature ->
                        when (feature) {
                            is CircleFeature -> {
                                val featureOffset: Offset = feature.position.toOffset()
                                if (isVisible(featureOffset)) {
                                    drawCircle(
                                        color = feature.color,
                                        radius = feature.radius.toPx(),
                                        center = featureOffset,
                                        style = feature.style,
                                    )
                                }
                                Unit
                            }

                            is LineFeature -> {
                                val start = feature.positionStart.toOffset()
                                val end = feature.positionEnd.toOffset()
                                if (isVisible(start, end)) {
                                    drawLine(
                                        color = feature.color,
                                        start = feature.positionStart.toOffset(),
                                        end = feature.positionEnd.toOffset(),
                                        strokeWidth = feature.width.toPx(),
                                        cap = feature.cap,
                                        pathEffect = feature.pathEffect
                                    )
                                }
                                Unit
                            }

                            is ImageFeature -> {
                                val offset = feature.position.toOffset() - feature.centerOffset.toOffset()
                                if (isVisible(offset)) {
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
                                Unit
                            }

                            is ScaledRectFeature -> {
                                val size = feature.size.toSizeAsValue() * scale.toFloat()
                                val offset = feature.position.toOffset()
                                if (isVisible(offset)) {
                                    drawRect(
                                        brush = feature.brush,
                                        topLeft = offset,
                                        size = size,
                                        style = feature.style
                                    )
                                }
                                Unit
                            }

                            is RectFeature -> {
                                val position = feature.position.toOffset()
                                val topLeft = position - feature.centerOffset.toOffset()
                                if (isVisible(topLeft)) {
                                    rotate(degrees = feature.rotationAngle, pivot = position) {
                                        drawRoundRect(
                                            brush = feature.brush,
                                            topLeft = topLeft,
                                            size = feature.size.toSize(),
                                            style = feature.style,
                                            cornerRadius = feature.cornerRadius,
                                        )
                                    }
                                }
                                Unit
                            }

                            is ScaledImageFeature -> {
                                val size = feature.size.toSizeAsValue() * scale.toFloat()
                                val offset = feature.position.toOffset()
                                if (isVisible(offset)) {
                                    translate(
                                        left = offset.x,
                                        top = offset.y
                                    ) {
                                        with(feature.painter) {
                                            draw(size = size)
                                        }
                                    }
                                }
                                Unit
                            }

                            is TextFeature -> if (!asyncText) {
                                val offset = feature.position.toOffset() - feature.centerOffset.toOffset()
                                if (isTextVisible(offset)) {
                                    drawIntoCanvas { canvas ->
                                        canvas.nativeCanvas.drawText1(
                                            string = feature.text,
                                            x = offset.x,
                                            y = offset.y,
                                            fontSize = feature.fontSize.toPx(),
                                            paint = Paint().apply { color = feature.color }
                                        )
                                    }
                                }
                                Unit
                            } else Unit
                        }.exhaustive()
                    }
                    if (viewData.value.showDebug) {
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height)
                        )
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2)
                        )
                    }
                }
            }
            if (asyncText) {
                val textFeatures = remember(features.value) {
                    features.value.filterIsInstance<TextFeature>().toImmutable()
                }
                DrawTextFeatures(
                    features = textFeatures,
                    viewData = viewData.value,
                    asyncRenderThreshold = 0.1
                )
            }
            if (viewData.value.showDebug) {

                Column(
                    modifier = Modifier
                        .background(color = Color.White.copy(alpha = .5f))
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


fun ViewData.isVisible(offset: Offset) =
    (offset.x in 0f..size.width) && (offset.y in 0f..size.height)

fun ViewData.isVisible(start: Offset, end: Offset) =
    isVisible(start) || isVisible(end)

fun ViewData.isTextVisible(offset: Offset) =
    (offset.x > -150f && offset.x < size.width)
            && (offset.y > 0f && offset.y < size.height)

fun DpSize.toSizeAsValue() = Size(width.value, height.value)


@Immutable
data class ImmutableList<T>(
    val list: List<T>,
)

fun <T> List<T>.toImmutable() = ImmutableList(this)
