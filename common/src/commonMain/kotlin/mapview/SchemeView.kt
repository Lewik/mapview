package mapview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch
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
    val scale by derivedStateOf { viewPoint.scale }
    val zoom by derivedStateOf { log2(scale) }
    val tileZoom by derivedStateOf { zoom.toInt() }
    val tileScale by derivedStateOf { 2.0.pow((zoom - tileZoom)) }
    println("AAA zoom $zoom tileZoom $tileZoom tileScale $tileScale (zoom - tileZoom) ${zoom - tileZoom}")
    val mapTiles = remember { mutableStateListOf<MapTile>() }
    val tileSize by derivedStateOf {
        if (mapTileProvider != null) {
            ceil(mapTileProvider.tileSize * tileScale).toInt()
        } else {
            0
        }
    }

    val canvasModifier = modifier
        .fillMaxSize()

    if (mapTileProvider !== null) (
            LaunchedEffect(viewPoint) {
                with(viewPoint) {
                    val topLeft = Offset.Zero.toSchemeCoordinates()// viewPoint.focus

//                    val range = -MapTileProvider.EQUATOR / 2..MapTileProvider.EQUATOR / 2
//                    if (topLeft.x !in range || topLeft.y !in range) println("WARNING: topLeft out of bounds")


                    val startTileId = calculate(topLeft, tileZoom)

                    var horizontalTileNum = size.width / tileSize
                    var verticalTileNum = size.height / tileSize

                    val tileIds = (0..(size.width / tileSize).toInt()).flatMap { additionalX ->
                        (0..(size.height / tileSize).toInt()).map { additionalY ->
                            startTileId.copy(
                                x = startTileId.x + additionalX,
                                y = startTileId.y + additionalY,
                            )
                        }
                    }

//                println("top left $center tileId $tileId")
                    println("tileIds (${tileIds.size}) $tileIds")
                    mapTiles.clear()
                    tileIds.forEach { tileId ->
                        launch {
                            mapTileProvider.loadTileAsync(startTileId)?.also {
                                mapTiles += it
                            }
                        }
                    }
                }
            }
            )

    Canvas(canvasModifier) {
        if (viewPoint.size != size) {
            onResize(size)
        }
        clipRect {
            if (mapTileProvider !== null) {
                val tileSizeXY = IntSize(
                    width = ceil(mapTileProvider.tileSize * tileScale).toInt(),
                    height = ceil(mapTileProvider.tileSize * tileScale).toInt()
                )
                mapTiles.forEach { (id, image) ->
                    val offset = with(viewPoint) {
                        calculateBack(id)
                            .also {
                                println("coord: $it, id: $id, tileZoom $tileZoom ${id.zoom}")
                            }
                            .toOffset()

                    }.let {
                        val tileNum = 2.0.pow(tileZoom)
                        println("tile $id tileNum $tileNum offset: $it")
                        IntOffset(it.x.toInt(), it.y.toInt())
                    }
                    println("int offset $offset, tileSizeXY $tileSizeXY")
                    drawImage(
                        image = image,
                        dstOffset = offset,
                        dstSize = tileSizeXY
                    )
                }
            }
            with(viewPoint) {
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

fun calculate(center: SchemeCoordinates, tileZoom: Int): TileId {
    val equator = MapTileProvider.EQUATOR
    val tileNum = 2.0.pow(tileZoom)
    val tileX = ((center.x + (equator / 2.0)) * tileNum / equator).toInt()
    val tileY = (-(center.y - (equator / 2.0)) * tileNum / equator).toInt()
    return TileId(tileZoom, tileX, tileY)
}

fun calculateBack(tileId: TileId): SchemeCoordinates {
    val equator = MapTileProvider.EQUATOR
    val tileNum = 2.0.pow(tileId.zoom)
    val x = tileId.x * equator / tileNum - equator / 2.0
    val y = -(tileId.y * equator / tileNum) + equator / 2.0
    return SchemeCoordinates(x, y)
}
