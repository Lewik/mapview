package mapview

import androidx.compose.ui.graphics.ImageBitmap

class MapTileProviderImpl(
    private val getTile: suspend (zoom: Int, x: Int, y: Int) -> ImageBitmap?,
    override val minScale: Int,
    override val maxScale: Int,
) : MapTileProvider {


    private suspend fun downloadImageAsync(tileId: TileId) =
        getTile(tileId.zoom, tileId.x, tileId.y)

    override suspend fun loadTile(
        tileId: TileId,
    ): MapTile? {
        val imageBitmap = downloadImageAsync(tileId)
        return if (imageBitmap != null) {
            MapTile(tileId, imageBitmap)
        } else {
            null
        }
    }

}
