package mapview

import androidx.compose.ui.graphics.ImageBitmap

class MapTileProviderImpl(
    private val getTile: suspend (zoom: Int, x: Int, y: Int) -> ImageBitmap?,
) : MapTileProvider {


    private suspend fun downloadImageAsync(tileId: TileId) =
        getTile(tileId.zoom, tileId.x, tileId.y)

    override suspend fun loadTileAsync(
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
