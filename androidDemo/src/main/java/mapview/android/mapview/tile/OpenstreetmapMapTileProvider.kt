package mapview.android.mapview.tile

import io.ktor.client.*
import io.ktor.client.engine.cio.*

open class OpenstreetmapMapTileProvider(
    url: (zoom: Int, x: Int, y: Int) -> String = Companion::osmUrl,
    client: HttpClient = HttpClient(CIO),
) : HttpMapTileProvider(
    url = url,
    client = client,
    parallel = 1
) {
    companion object {
        fun osmUrl(zoom: Int, x: Int, y: Int) = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
    }
}
