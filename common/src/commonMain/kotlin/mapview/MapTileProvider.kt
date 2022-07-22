package mapview

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.floor

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
        const val SHIFT = 20037508.342789244
    }
}
