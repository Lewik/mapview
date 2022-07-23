package mapview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


private fun IntRange.intersect(other: IntRange) = max(first, other.first)..min(last, other.last)


@Composable
fun SchemeView(
    mapTileProvider: MapTileProvider? = null,
//    computeViewData: (canvasSize: DpSize) -> MapViewData,
//    features: Map<FeatureId, MapFeature>,
//    onClick: MapViewData.() -> Unit,
//    config: MapViewConfig,
    features: List<Feature>,
    viewData: ViewData,
    onViewDataChange: (viewData: ViewData) -> Unit,
    onResize: (size: Size) -> Unit,
    modifier: Modifier,
) {
    val scale by derivedStateOf { viewData.scale }

    val zoom by derivedStateOf {
        if (mapTileProvider != null) {
            (1..20).first { zoom ->
                val totalTiles = 2.0.pow(zoom)
                val scaledMapSize = MapTileProvider.EQUATOR * scale
                val scaledTileSize = scaledMapSize / totalTiles
                scaledTileSize < mapTileProvider.tileSize
            }
        } else {
            0
        }
    }
    val tileNum by derivedStateOf { 2.0.pow(zoom).toInt() }
    val tileSize by derivedStateOf { (MapTileProvider.EQUATOR * scale / tileNum).toInt() }
    println("AAA zoom $zoom tileNum $tileNum tileSize $tileSize")
    val mapTiles = remember { mutableStateListOf<MapTile>() }


    val canvasModifier = modifier
        .fillMaxSize()

    if (mapTileProvider !== null) (
            LaunchedEffect(viewData) {
                with(viewData) {
                    val topLeft = Offset.Zero.toSchemeCoordinates()

//                    val range = -MapTileProvider.EQUATOR / 2..MapTileProvider.EQUATOR / 2
//                    if (topLeft.x !in range || topLeft.y !in range) println("WARNING: topLeft out of bounds")

                    val startTileId = calculate(topLeft, zoom)
                    val tileRange = 0..tileNum
                    val unfilteredTileIds = (0..(size.width / tileSize).toInt())
                        .flatMap { additionalX ->
                            (0..(size.height / tileSize).toInt())
                                .map { additionalY ->
                                    startTileId.copy(
                                        x = startTileId.x + additionalX,
                                        y = startTileId.y + additionalY,
                                    )
                                }
                        }

                    val (tileIds, outOfRangeTiles) = unfilteredTileIds.partition { it.x in tileRange && it.y in tileRange }
                    if (outOfRangeTiles.isNotEmpty()) {
                        println("outOfRangeTiles $outOfRangeTiles")
                    }

                    println("tileIds (${tileIds.size}) (0..${size.width / tileSize} 0..${size.height / tileSize}) $tileIds")
                    mapTiles.clear()
                    tileIds.forEach { tileId ->
                        launch {
                            try {
                                mapTileProvider.loadTileAsync(tileId)?.also {
                                    mapTiles += it
                                }
                            } catch (e: Exception) {
                                println("WARINIG")
                                println(e)
                            }
                        }
                    }
                }
            }
            )

    Canvas(canvasModifier) {
        if (viewData.size != size) {
            onResize(size)
        }
        clipRect {
            if (mapTileProvider !== null) {
                val tileSizeXY = IntSize(
                    width = tileSize,
                    height = tileSize
                )
                mapTiles.forEach { (id, image) ->
                    val offset = with(viewData) {
                        calculateBack(id)
                            .also {
//                                println("coord: $it, id: $id, zoom $zoom ${id.zoom}")
                            }
                            .toOffset()

                    }.let {
                        IntOffset(it.x.toInt(), it.y.toInt())
                    }
                    println("tile $id tileNum $tileNum int offset $offset, tileSizeXY $tileSizeXY")
                    drawImage(
                        image = image,
                        dstOffset = offset,
                        dstSize = tileSizeXY
                    )
                    drawRect(
                        color = Color.Red,
                        topLeft = offset.toOffset(),
                        size = tileSizeXY.toSize(),
                        style = Stroke(width = 1f)
                    )
                }
            }
            with(viewData) {
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
                    }.exhaustive()

                }
            }
        }
    }
}

fun calculate(center: SchemeCoordinates, zoom: Int): TileId {
    val equator = MapTileProvider.EQUATOR
    val tileNum = 2.0.pow(zoom)
    val tileX = ((center.x + (equator / 2.0)) * tileNum / equator).toInt()
    val tileY = (-(center.y - (equator / 2.0)) * tileNum / equator).toInt()
    return TileId(zoom, tileX, tileY)
}

fun calculateBack(tileId: TileId): SchemeCoordinates {
    val equator = MapTileProvider.EQUATOR
    val tileNum = 2.0.pow(tileId.zoom)
    val x = tileId.x * equator / tileNum - equator / 2.0
    val y = -(tileId.y * equator / tileNum) + equator / 2.0
    return SchemeCoordinates(x, y)
}
