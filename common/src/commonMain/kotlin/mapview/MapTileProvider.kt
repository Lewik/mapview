package mapview

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.floor
import kotlin.math.pow

data class TileId(
    val zoom: Int,
    val x: Int,
    val y: Int,
)

data class MapTile(
    val id: TileId,
    val image: ImageBitmap,
)

interface MapTileProvider {
    suspend fun loadTileAsync(tileId: TileId): MapTile?

    val tileSize: Int get() = DEFAULT_TILE_SIZE

    fun toIndex(d: Double): Int = floor(d / tileSize).toInt()

    fun toCoordinate(i: Int): Double = (i * tileSize).toDouble()

    companion object {
        const val DEFAULT_TILE_SIZE = 256

        //        const val EQUATOR = 20026376.39 * 2
//        const val X = 20026376.39 * 2
//        const val Y = 20048966.10 * 2
//        const val SHIFT = 20037508.342789244
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
