package mapview

class MapTileProviderImpl(
    private val getTile: suspend (zoom: Int, x: Int, y: Int) -> ByteArray,
) : MapTileProvider {


    private suspend fun downloadImageAsync(tileId: TileId) =
        getTile(tileId.zoom, tileId.x, tileId.y)

    override suspend fun loadTileAsync(
        tileId: TileId,
    ): MapTile {
        val image = downloadImageAsync(tileId).toImageBitmap()
        return MapTile(tileId, image)
    }

}
