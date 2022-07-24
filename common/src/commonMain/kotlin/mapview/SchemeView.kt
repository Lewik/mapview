package mapview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch
import mapview.viewData.ViewData
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.*


private fun IntRange.intersect(other: IntRange) = max(first, other.first)..min(last, other.last)


@Composable
fun SchemeView(
    mapTileProvider: MapTileProvider? = null,
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
) {
    val viewData by derivedStateOf { viewDataState.value } //TODO is it correct?
    with(LocalDensity.current) {
        val scale by derivedStateOf { viewData.scale }

        val zoom by derivedStateOf {
            if (mapTileProvider != null) {
                (mapTileProvider.minScale..mapTileProvider.maxScale).firstOrNull { zoom ->
                    val totalTiles = 2.0.pow(zoom)
                    val scaledMapSize = MapTileProvider.EQUATOR * scale
                    val scaledTileSize = scaledMapSize / totalTiles
                    scaledTileSize < mapTileProvider.tileSize.toPx()
                } ?: mapTileProvider.maxScale
            } else {
                0
            }
        }
        val tileNum by derivedStateOf { 2.0.pow(zoom).toInt() }
        val tileSize by derivedStateOf { ceil(MapTileProvider.EQUATOR * scale / tileNum).toInt() }
//        println("TEST zoom $zoom tileNum $tileNum tileSize $tileSize")
        val mapTiles = remember { mutableStateListOf<MapTile>() }


        if (mapTileProvider !== null) {
            LaunchedEffect(viewData) {
                with(viewData) {
                    val topLeft = Offset.Zero.toSchemeCoordinates()

//                    val range = -MapTileProvider.EQUATOR / 2..MapTileProvider.EQUATOR / 2
//                    if (topLeft.x !in range || topLeft.y !in range) println("WARNING: topLeft out of bounds")

                    val tileRange = 0..tileNum
                    val startTileId = topLeft
                        .toTileId(zoom)
                        .coerceInTileRange(tileRange)
                    val tileXRange = (0..ceil(size.width / tileSize).toInt())
                        .intersect(tileRange)
                    val tileYRange = (0..ceil(size.height / tileSize).toInt())
                        .intersect(tileRange)

                    val tileIds = tileXRange
                        .flatMap { additionalX ->
                            tileYRange
                                .map { additionalY ->
                                    startTileId.copy(
                                        x = startTileId.x + additionalX,
                                        y = startTileId.y + additionalY,
                                    )
                                }
                        }


//                    println("tileIds (${tileIds.size}) (0..${size.width / tileSize} 0..${size.height / tileSize}) $tileIds")
                    mapTiles.clear()

                    val centerTileX = (startTileId.x + tileXRange.first + startTileId.x + tileXRange.last) / 2
                    val centerTileY = (startTileId.y + tileYRange.first + startTileId.y + tileYRange.last) / 2

                    launch {
                        tileIds
                            .sortedBy { hypot((centerTileX - it.x).toDouble(), (centerTileY - it.y).toDouble()) }
                            .forEach { tileId ->
                                try {
                                    mapTileProvider.loadTile(tileId)?.also {
                                        mapTiles += it
                                    }
                                } catch (e: Exception) {
                                    if (e !is CancellationException) {
                                        println("WARINIG")
                                        println(e)
                                    }
                                }
                            }
                    }
                }
            }
        }

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
                with(viewData) {
                    if (mapTileProvider !== null) {
                        val tileSizeXY = IntSize(
                            width = tileSize,
                            height = tileSize
                        )
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
                                    color = Color.Red,
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
                                        paint = Paint().apply { color = Color.Red }
                                    )
                                    it.nativeCanvas.drawText1(
                                        string = "y: ${tileId.y}",
                                        x = offset.x + 20.dp.toPx(),
                                        y = offset.y + 40.dp.toPx(),
                                        fontSize = 10.dp.toPx(),
                                        paint = Paint().apply { color = Color.Red }
                                    )
                                    it.nativeCanvas.drawText1(
                                        string = "zoom: ${tileId.zoom}",
                                        x = offset.x + 20.dp.toPx(),
                                        y = offset.y + 60.dp.toPx(),
                                        fontSize = 10.dp.toPx(),
                                        paint = Paint().apply { color = Color.Red }
                                    )
                                }
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
        }

        if (viewData.showDebug) {
            Box {
                Column(
                    modifier = Modifier
                        .background(color = Color.White.copy(alpha = .5f))
                        .padding(10.dp)
                ) {
                    Text("Focus: x: ${viewData.focus.x}, y: ${viewData.focus.x}")
                    Text("Scale: ${viewData.scale}, min: ${viewData.getMinScaleCoerce()}, max: ${viewData.maxScale}")
                    if (mapTileProvider != null) {
                        Text("Zoom: $zoom")
                    }
                    Text("Size: width:${viewData.size.width}, height: ${viewData.size.height}")
                }
            }
        }
    }
}

