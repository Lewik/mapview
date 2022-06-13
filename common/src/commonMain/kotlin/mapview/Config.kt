package mapview

val TILE_SIZE = 512

object Config {
    const val DISPLAY_TELEMETRY: Boolean = true
    const val SIMULATE_NETWORK_PROBLEMS = false
    const val CLICK_DURATION_MS: Long = 300
    const val CLICK_AREA_RADIUS_PX: Int = 7
    const val ZOOM_ON_CLICK = 0.8
    const val MAX_SCALE_ON_SINGLE_ZOOM_EVENT = 2.0
    const val SCROLL_SENSITIVITY_DESKTOP = 0.05
    const val SCROLL_SENSITIVITY_BROWSER = 0.001
    const val CACHE_DIR_NAME = "map-view-cache"
    const val MIN_ZOOM = 0
    const val MAX_ZOOM = 22
    const val FONT_LEVEL = 2

    fun createTileUrl(zoom: Int, x: Int, y: Int, mapTilerSecretKey: String): String =
        "https://api.maptiler.com/maps/streets/$zoom/$x/$y.png?key=$mapTilerSecretKey"
}

/**
 * MapTiler tile,
 * doc here https://cloud.maptiler.com/maps/streets/
 */
data class Tile(
    val zoom: Int,
    val x: Int,
    val y: Int,
)
