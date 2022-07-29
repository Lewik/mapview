package mapview.tiles

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mapview.view.TILE_SIZE

open class MapTileProvider(
    parallel: Int = 10,
    /**
     * use ByteArray.toImageBitmap() from composeUtils.kt
     */
    private val load: suspend (zyx: Zyx) -> ImageBitmap?,
) {
    private val semaphore = Semaphore(parallel)
    suspend fun loadTile(tileId: TileId): MapTile? = semaphore.withPermit {
        val result = load(Zyx(tileId.zoom, tileId.x, tileId.y))
        if (result != null) {
            MapTile(tileId, result, TILE_SIZE, 0, 0)
        } else {
            null
        }
    }

    data class Zyx(
        val z: Int,
        val x: Int,
        val y: Int,
    )

    companion object {
        /**
         * set parallel = 1 for osm
         */
        fun osmUrl(zyx: Zyx) = "https://tile.openstreetmap.org/${zyx.z}/${zyx.x}/${zyx.y}.png"
    }
}
