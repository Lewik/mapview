package mapview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.*


private fun IntRange.intersect(other: IntRange) = max(first, other.first)..min(last, other.last)


@Composable
fun SchemeView(
    mapTileProvider: MapTileProvider? = null,
//    computeViewPoint: (canvasSize: DpSize) -> MapViewPoint,
//    features: Map<FeatureId, MapFeature>,
//    onClick: MapViewPoint.() -> Unit,
//    config: MapViewConfig,
    features: List<Feature>,
    viewPoint: ViewPoint,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    onResize: (size: Size) -> Unit,
    modifier: Modifier,
) {
//
//    var canvasSize by remember { mutableStateOf(Size(512f, 512f)) }

//    val scale by derivedStateOf { viewPoint.scale.toFloat() }

    val scale by derivedStateOf { viewPoint.scale }
    val zoom by derivedStateOf { log2(scale) }
    val tileZoom by derivedStateOf { zoom.toInt() }
    val tileScale by derivedStateOf { 2.0.pow((zoom - tileZoom)) }
//
//
//    val centerCoordinates by derivedStateOf { viewPoint.focus }
//
    val mapTiles = remember { mutableStateListOf<MapTile>() }
    val canvasModifier = modifier
        .fillMaxSize()

    if (mapTileProvider !== null) (
            LaunchedEffect(viewPoint) {
                with(viewPoint) {


//                    val topLeft = Offset.Zero.toSchemeCoordinates()
                    val center = viewPoint.focus
                    if (center.x !in -MapTileProvider.SHIFT..MapTileProvider.SHIFT) {
                        println("WARNING: topLeft out of bounds")
                    }
                    if (center.y !in -MapTileProvider.SHIFT..MapTileProvider.SHIFT) {
                        println("WARNING: topLeft out of bounds")
                    }
                    val tileNum = 2.0.pow(tileZoom)
//                    x = [1 + (x / π)] / 2
//                    y = [1 − (y / π)] / 2
//                    val tileX = ((1 + topLeft.x/MapTileProvider.SHIFT / PI) / 2 * tileNum).toInt()
//                    val tileY =( (1 - topLeft.y/MapTileProvider.SHIFT / PI) / 2* tileNum).toInt()
//                    var equator = 40075016.68557849;
//                    var pixelX = (p.x + (equator / 2.0)) / (equator / 256.0);
//                    var pixelY = ((p.y -(equator / 2.0)) / (equator / -256.0));
//                    return L.point(pixelX, pixelY);
                    val (tileX, tileY) = calculate(center, tileZoom)
//                    val tileX = (topLeft.x * tileNum / MapTileProvider.X).toInt()
//                    val tileY = ((MapTileProvider.Y - topLeft.y) * tileNum / MapTileProvider.Y).toInt()

                    println("top left $center tile $tileX, $tileY")

//
                    with(mapTileProvider) {

//                        val indexRange = 0 until scale.toInt()
//                        val left = centerCoordinates.x - canvasSize.width.value / 2 / tileScale
//                        val right = centerCoordinates.x + canvasSize.width.value / 2 / tileScale
//                        val horizontalIndices: IntRange = (toIndex(left)..toIndex(right)).intersect(indexRange)
//
//                        val top = (centerCoordinates.y + canvasSize.height.value / 2 / tileScale)
//                        val bottom = (centerCoordinates.y - canvasSize.height.value / 2 / tileScale)
//                        val verticalIndices: IntRange = (toIndex(bottom)..toIndex(top)).intersect(indexRange)

                        mapTiles.clear()
                        loadTileAsync(TileId(tileZoom, tileX, tileY))?.also {
                            mapTiles += it
                        }

//                        for (y in verticalIndices) {
//                            for (x in horizontalIndices) {
//                                val id = TileId(zoom, x, y)
////                            launch {
//                                try {
//                                    mapTiles += loadTileAsync(id)
//                                } catch (ex: Exception) {
//                                    if (ex !is CancellationException) {
//                                        println("Failed to load tile with id=$id")
//                                    }
//                                }
////                            }
//                            }
//                        }
                    }
                }
            }
            )

    Canvas(canvasModifier) {
//        fun SchemeCoordinates.toOffset(): Offset = Offset(
//            ((x - viewPoint.focus.x) * scale).toFloat() + viewPoint.size.width / 2,
//            -((y - viewPoint.focus.y) * scale).toFloat() + viewPoint.size.height / 2,
////            (canvasSize.width / 2 + (x.dp - centerCoordinates.x.dp) * tileScale).toPx(),
////            (canvasSize.height / 2 + (y.dp - centerCoordinates.y.dp) * tileScale).toPx()
//        )

        if (viewPoint.size != size) {
            onResize(size)
        }
        clipRect {
            if (mapTileProvider !== null) {
                val tileSize = IntSize(
                    width = ceil(mapTileProvider.tileSize * tileScale).toInt(),
                    height = ceil(mapTileProvider.tileSize * tileScale).toInt()
//                    ceil((mapTileProvider.tileSize.dp * tileScale.toFloat()).toPx()).toInt(),
//                    ceil((mapTileProvider.tileSize.dp * tileScale.toFloat()).toPx()).toInt()
                )
                mapTiles.forEach { (id, image) ->
                    val tileNum = 2.0.pow(tileZoom)

                    val offset = with(viewPoint) {
                        SchemeCoordinates(
                            x = (MapTileProvider.SHIFT * 2 / tileNum * id.x) - (MapTileProvider.SHIFT ),
                            y = -((MapTileProvider.SHIFT * 2 / tileNum * id.y) - (MapTileProvider.SHIFT )),
                        )
                            .also {
                                println("test x: ${it.x + MapTileProvider.SHIFT}")
                                println("test y: ${it.y - MapTileProvider.SHIFT}")
                                println("coord: $it")

                            }.toOffset()
                    }.let {
                        println("tile $id tileNum $tileNum offset: $it")
                        IntOffset(it.x.toInt(), it.y.toInt())
                    }
                    println("int offset $offset")
                    drawImage(
                        image = image,
//                        dstOffset = offset,
                        dstSize = tileSize
                    )
                }
            }
            with(viewPoint) {
                features.forEach { feature ->
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
            }
        }
    }
}

fun calculate(center: SchemeCoordinates, zoom: Int): Pair<Int, Int> {
    val equator = 40075016.68557849

    val tileSize = 256.0

    val tileX = (

            (center.x + (equator / 2.0)) /
                    (equator / tileSize)

            ).toInt()
    val tileY = (

            (center.y - (equator / 2.0)) /
                    (equator / -tileSize)

            ).toInt()
    return Pair(tileX, tileY)
}
