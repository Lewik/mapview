package mapview.tile

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mapview.tiles.MapTile
import mapview.tiles.MapTileProvider
import mapview.tiles.TileId
import mapview.toImageBitmap

open class HttpMapTileProvider(
    private val url: (zoom: Int, x: Int, y: Int) -> String,
    private val client: HttpClient = HttpClient(CIO),
    parallel: Int = 10,
) : MapTileProvider {
    private val semaphore = Semaphore(parallel)
    override suspend fun loadTile(tileId: TileId): MapTile? = semaphore.withPermit {
        val zoom = tileId.zoom
        val x = tileId.x
        val y = tileId.y

        val url = url(zoom, x, y)
        val result = client.get(url)
        if (result.status.isSuccess()) {
            MapTile(tileId, result.readBytes().toImageBitmap())
        } else {
            println("WARNING KTOR can't get $zoom/$x/$y ")
            null
        }
    }

    companion object {
        fun getOpenstreetmapUrl(zoom: Int, x: Int, y: Int) = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
    }

}


