package mapview

import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.roundToInt


fun InternalMapState.calcTiles(): List<DisplayTileAndTile> {
    val zoom = minOf(
        Config.MAX_ZOOM,
        maxOf(
            Config.MIN_ZOOM,
            ceil(log2(geoLengthToDisplay(1.0) / TILE_SIZE.toDouble())).roundToInt() - Config.FONT_LEVEL
        )
    )
    val maxTileIndex = pow2(zoom)
    val tileSize: Int = geoLengthToDisplay(1.0) / maxTileIndex + 1
    val minI = (topLeft.x * maxTileIndex).toInt()
    val minJ = (topLeft.y * maxTileIndex).toInt()

    val tiles: List<DisplayTileAndTile> = buildList {
        for (i in minI until Int.MAX_VALUE) {
            val geoX = i.toDouble() / maxTileIndex
            val displayX = geoXToDisplay(geoX)
            if (displayX >= width) {
                break
            }
            for (j in minJ until Int.MAX_VALUE) {
                val geoY = j.toDouble() / maxTileIndex
                val displayY = geoYToDisplay(geoY)
                if (displayY >= height) {
                    break
                }
                val tile = Tile(
                    zoom = zoom,
                    x = i % maxTileIndex,
                    y = j % maxTileIndex
                )
                add(
                    DisplayTileAndTile(
                        display = DisplayTile(
                            size = tileSize,
                            x = displayX,
                            y = displayY
                        ),
                        tile = tile
                    )
                )
            }
        }
    }
    return tiles
}
