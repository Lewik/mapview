@file:OptIn(ExperimentalTime::class)

package mapview

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

fun createRealRepository(ktorClient: HttpClient) =
    object : ContentRepository<Tile, ByteArray> {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override suspend fun loadContent(tile: Tile): ByteArray {
            if (Config.SIMULATE_NETWORK_PROBLEMS) {
                delay(0.5.seconds)
            }
            return ktorClient.get(
                urlString = Config.createTileUrl(tile.zoom, tile.x, tile.y)
            )
        }
    }

