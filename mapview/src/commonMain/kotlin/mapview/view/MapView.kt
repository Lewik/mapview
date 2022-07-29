package mapview.view

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mapview.Feature
import mapview.LruCache
import mapview.tiles.*
import mapview.viewData.ViewData
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.*


private fun IntRange.intersect(other: IntRange) = max(first, other.first)..min(last, other.last)


@Composable
fun MapView(
    mapTileProvider: State<MapTileProvider>,
    minZoom: Int = 1,
    maxZoom: Int = 18,
    tileSize: Dp = 256.dp,
    inmemoryTileCacheAmount: Int = 500,
    features: State<List<Feature>>,
    viewData: State<ViewData>,
    onDragStart: (offset: Offset) -> Unit = {},
    onDrag: (dragAmount: Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onScroll: (scrollY: Float, target: Offset?) -> Unit = { _, _ -> },
    onClick: (offset: Offset) -> Unit = {},
    onResize: (size: Size) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tileCache by remember {
        mutableStateOf(//state лишний?
            LruCache<TileId, MapTile>(inmemoryTileCacheAmount)
        )
    }

    with(LocalDensity.current) {
        with(viewData.value) {
            val zoom by remember {
                derivedStateOf {
                    (minZoom..maxZoom).firstOrNull { zoom ->
                        val totalTiles = 2.0.pow(zoom)
                        val scaledMapSize = EQUATOR * viewData.value.scale
                        val scaledTileSize = scaledMapSize / totalTiles
                        scaledTileSize < tileSize.toPx()
                    } ?: maxZoom
                }
            }
            val tileNum by remember {
                derivedStateOf {
                    2.0.pow(zoom).toInt()
                }
            }
            val scaledTileSize by remember {
                derivedStateOf {
                    ceil(EQUATOR * viewData.value.scale / tileNum).toInt()
                }
            }
//        println("TEST zoom $zoom tileNum $tileNum tileSize $tileSize")
            val mapTiles = remember { mutableStateListOf<MapTile>() }
            val tileSizeXY = remember {
                derivedStateOf {
                    IntSize(
                        width = scaledTileSize,
                        height = scaledTileSize
                    )
                }
            }
            LaunchedEffect(viewData.value) {
                val topLeft = Offset.Zero.toSchemeCoordinates()

//                    val range = -MapTileProvider.EQUATOR / 2..MapTileProvider.EQUATOR / 2
//                    if (topLeft.x !in range || topLeft.y !in range) println("WARNING: topLeft out of bounds")

                val tileRange = 0..tileNum
                val startTileId = topLeft
                    .toTileId(zoom)
                    .coerceInTileRange(tileRange)
                val tileXRange = (0..ceil(size.width / scaledTileSize).toInt())
                    .intersect(tileRange)
                val tileYRange = (0..ceil(size.height / scaledTileSize).toInt())
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

                tileIds
                    .sortedBy { hypot((centerTileX - it.x).toDouble(), (centerTileY - it.y).toDouble()) }
                    .forEach { tileId ->
                        val cachedTile = tileCache[tileId]
                        if (cachedTile != null) {
                            mapTiles += cachedTile
                        } else {
//                            val croppedTile = tileCache.searchOrCrop(tileId)
//                            if (croppedTile != null) {
//                                mapTiles += croppedTile
//                            }
                            launch {
                                try {
                                    val loadedTile = mapTileProvider.value.loadTile(tileId)
                                    val tile = if (loadedTile != null) {
                                        tileCache[tileId] = loadedTile
                                        loadedTile
                                    } else {
                                        null
                                    }
                                    if (tile != null) {
                                        mapTiles += tile
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


            AbstractView(
                features = features,
                viewData = viewData,
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                onScroll = onScroll,
                onClick = onClick,
                onResize = onResize,
                modifier = modifier,
                tileSizeXY = tileSizeXY.value,
                mapTiles = mapTiles
            )
        }
    }
}

const val TILE_SIZE = 256

fun LruCache<TileId, MapTile>.searchOrCrop(tile: TileId): MapTile? {
    val img1 = get(tile)
    if (img1 != null) {
        return img1
    }
    var zoom = tile.zoom
    var x = tile.x
    var y = tile.y
    while (zoom > 0) {
        zoom--
        x /= 2
        y /= 2
        val tile2 = TileId(zoom, x, y)
        val img2 = get(tile2)
        if (img2 != null) {
            val deltaZoom = tile.zoom - tile2.zoom
            val i = tile.x - (x shl deltaZoom)
            val j = tile.y - (y shl deltaZoom)
            val size = max(TILE_SIZE ushr deltaZoom, 1)
            println("//TODO : 256!")
            return img2.cropAndRestoreSize(i * size, j * size, size)
        }
    }
    return null
}

fun MapTile.cropAndRestoreSize(x: Int, y: Int, targetSize: Int): MapTile {
    val scale: Float = targetSize.toFloat() / TILE_SIZE
    val newSize = maxOf(1, (cropSize * scale).roundToInt())
    val dx = x * newSize / targetSize
    val dy = y * newSize / targetSize
    val newX = offsetX + dx
    val newY = offsetY + dy
    return copy(
        cropSize = newX % TILE_SIZE,
        offsetX = newY % TILE_SIZE,
        offsetY = newSize
    )
}
