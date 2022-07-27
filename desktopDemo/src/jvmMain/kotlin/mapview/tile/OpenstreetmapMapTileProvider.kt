package mapview.tile

import io.ktor.client.*
import io.ktor.client.engine.cio.*

open class OpenstreetmapMapTileProvider(
    private val url: (zoom: Int, x: Int, y: Int) -> String = Companion::OsmUrl,
    private val client: HttpClient = HttpClient(CIO),
) : HttpMapTileProvider(
    url = url,
    client = client,
    parallel = 1
) {
    companion object {
        fun OsmUrl(zoom: Int, x: Int, y: Int) = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
    }

}
