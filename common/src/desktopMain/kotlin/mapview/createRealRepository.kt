package mapview

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlin.random.Random

fun createRealRepository(ktorClient: HttpClient, mapTilerSecretKey: String) =
    object : ContentRepository<Tile, ByteArray> {
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        override suspend fun loadContent(tile: Tile): ByteArray {
            if (Config.SIMULATE_NETWORK_PROBLEMS) {
                delay(Random.nextLong(0, 100))
                if (Random.nextInt(100) < 10) {
                    throw Exception("Simulate network problems")
                }
                delay(Random.nextLong(0, 3000))
            }
            return ktorClient.get(
                urlString = Config.createTileUrl(tile.zoom, tile.x, tile.y, mapTilerSecretKey)
            )
        }
    }

