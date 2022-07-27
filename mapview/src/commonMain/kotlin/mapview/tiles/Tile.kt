package mapview.tiles

import androidx.compose.ui.graphics.ImageBitmap
import mapview.SchemeCoordinates
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
    val cropSize: Int,
    val offsetX: Int,
    val offsetY: Int,
)

interface MapTileProvider {
    suspend fun loadTile(tileId: TileId): MapTile?

    companion object {
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
