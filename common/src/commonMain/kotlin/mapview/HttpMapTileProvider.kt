package mapview

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.pow

data class TileId(
    val zoom: Int,
    val x: Int,
    val y: Int,
)

fun TileId.coerceInTileRange(tileRange: IntRange) = copy(
    x = x.coerceIn(tileRange),
    y = y.coerceIn(tileRange),
)


data class MapTile(
    val id: TileId,
    val image: ImageBitmap,
)

interface MapTileProvider {
    suspend fun loadTile(tileId: TileId): MapTile?

    val minScale: Int
    val maxScale: Int

    val tileSize: Dp get() = DEFAULT_TILE_SIZE

    companion object {
        val DEFAULT_TILE_SIZE = 256.dp

        const val EQUATOR = 40075016.68557849
    }
}


fun SchemeCoordinates.toTileId(zoom: Int): TileId {
    val equator = MapTileProvider.EQUATOR
    val tileNum = 2.0.pow(zoom)
    val tileX = ((x + (equator / 2.0)) * tileNum / equator).toInt()
    val tileY = (-(y - (equator / 2.0)) * tileNum / equator).toInt()
    return TileId(zoom, tileX, tileY)
}

fun TileId.toSchemaCoordinates(): SchemeCoordinates {
    val equator = MapTileProvider.EQUATOR
    val tileNum = 2.0.pow(zoom)
    val x = x * equator / tileNum - equator / 2.0
    val y = -(y * equator / tileNum) + equator / 2.0
    return SchemeCoordinates(x, y)
}
