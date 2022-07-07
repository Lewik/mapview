package mapview

import mapview.GlobalMercator.getTopLeftMapPixel
import mapview.GlobalMercator.toDisplayPixel
import mapview.GlobalMercator.toMeters
import mapview.GlobalMercator.toTileXY
import mapview.GlobalMercator.zoomToScaleWithinZoom

//fun InternalMapState.calcTiles(): List<DisplayTileAndTile> {
//    fun geoLengthToDisplay(geoLength: Double): Int {
//        return (height * geoLength * zoom).toInt()
//    }
//
//    val tileZoom = zoom.toInt()
//    val maxTileIndex: Int = pow2(tileZoom)
//    val tileSize: Int = geoLengthToDisplay(1.0) / maxTileIndex + 1
//    val minI = (topLeftOffset.x * maxTileIndex).toInt()
//    val minJ = (topLeftOffset.y * maxTileIndex).toInt()
//
//    fun geoXToDisplay(x: Double): Int = geoLengthToDisplay(x - topLeftOffset.x)
//    fun geoYToDisplay(y: Double): Int = geoLengthToDisplay(y - topLeftOffset.y)
//
//    val tiles: List<DisplayTileAndTile> = buildList {
//        for (i in minI until Int.MAX_VALUE) {
//            val geoX = i.toDouble() / maxTileIndex
//            val displayX = geoXToDisplay(geoX)
//            if (displayX >= width) {
//                break
//            }
//            for (j in minJ until Int.MAX_VALUE) {
//                val geoY = j.toDouble() / maxTileIndex
//                val displayY = geoYToDisplay(geoY)
//                if (displayY >= height) {
//                    break
//                }
//                val tile = Tile(tileZoom, i % maxTileIndex, j % maxTileIndex)
//                add(
//                    DisplayTileAndTile(
//                        DisplayTile(tileSize, DisplayPixel(displayX.toDouble(), displayY.toDouble())),
//                        tile
//                    )
//                )
//            }
//        }
//    }
//    return tiles
//}

fun InternalMapState.calcTiles(): List<DisplayTileAndTile> {
    val tileZoom = zoom.toInt()
//    val maxTileIndex = pow2(tileZoom)
//    val mapPixelSize = TILE_SIZE * 1.0.pow(this.zoom)
    val tileSize = (TILE_SIZE * zoom.zoomToScaleWithinZoom()).toInt()
//    println("tileSize $tileSize zoom $zoom scale ${zoom.zoomToScale()}")

    val (minX, minY) = topLeftOffset.toMeters(zoom).toTileXY()
//    val (maxX, maxY) = topLeftOffset.plus(DisplayPixel(width.toDouble(), height.toDouble())).toMeters(zoom).toTileXY(tileZoom)
    val maxTileIndex = pow2(tileZoom)
    val maxYNum = height.floorDiv(tileSize)
    val maxXNum = width.floorDiv(tileSize)
    println("topLeftOffset $topLeftOffset minX $minX minY $minY maxXNum $maxXNum maxYNum $maxYNum")

    val tiles = (minY..minY + maxYNum)
        .flatMap { y ->
            (minX..minX + maxXNum)
                .map { x ->
                    TileXY(x = x % maxTileIndex, y = y % maxTileIndex)
                }
        }
        .toSet()
        .also { println("tiles $it") }
        .filter {
            it.x >= 0 && it.y >= 0 && it.x < maxTileIndex && it.y < maxTileIndex
        }
        .also {
            println("tiles: ${it.size} maxTileIndex: $maxTileIndex")
            println("tiles $it")
        }
        .map { (x, y) ->
            DisplayTileAndTile(
                display = DisplayTile(
                    size = tileSize,
                    displayPixel = TileXY(x, y)
                        .getTopLeftMapPixel()
                        .toDisplayPixel(zoom)
//                        .also { println(("pix $it")) }
                        .plus(topLeftOffset)
                ),
                tile = Tile(
                    zoom = tileZoom,
                    x = x,
                    y = y
                )
            )

        }
    return tiles


//    val pixelCenter = topLeftOffset
//        .plus(getCenterOffset())
//        .toMapPixel(zoom)
////    val zoomedCenter = pixelCenter.toZoomedMapPixel(zoom)
//    val offsetX = topLeftOffset.x
//    val offsetY = topLeftOffset.y
//    val tiles = GlobalMercator
//        .getTiles(
//            mapPixel = pixelCenter,
//            zoom = tileZoom,
//            height = width,
//            width = height
//        )
//        .map {
//            val topLeftMapPixel = it
//                .getTopLeftMapPixel()
//            val zoomedMapPixel = topLeftMapPixel
//                .toDisplayPixel(zoom - zoom.toInt())
////            println("X: ${topLeftMapPixel.x} -> ${zoomedMapPixel.x}")
//
//            DisplayTileAndTile(
//                display = DisplayTile(
//                    size = tileSize,
//                    displayPixel = zoomedMapPixel
//                        .move(offsetX, offsetY)
//                ),
//                tile = Tile(
//                    zoom = tileZoom,
//                    x = it.x,
//                    y = it.y
//                )
//            )
//        }.also {
//            println(it.joinToString { "${it.tile} ${it.display.displayPixel}" })
//        }

//    val tileZoom1 = minOf(Config.MAX_ZOOM, maxOf(Config.MIN_ZOOM, ceil(log2(height * zoom / TILE_SIZE.toDouble())).roundToInt())) - 2
////    val tileZoom = minOf(Config.MAX_ZOOM, maxOf(Config.MIN_ZOOM, scale.toInt()))
//    val tileZoom = minOf(Config.MAX_ZOOM, maxOf(Config.MIN_ZOOM, ceil(log2(zoom)).roundToInt()))
//    val maxTileIndex = pow2(tileZoom)
//
//    val mapPixelSize = TILE_SIZE * zoom
//    val tileSize = (mapPixelSize / maxTileIndex).toInt()
//    println("scale $zoom tileZoom1 $tileZoom1 tileZoom $tileZoom tileSize $tileSize this.center ${this.center}")
//
//    val tilex = ((1 + (this.center.x / PI)) / 2 * maxTileIndex).toInt()
//    val tiley = ((1 - (this.center.y / PI)) / 2 * maxTileIndex).toInt()
//
//
////    val minX = (topLeft.x * maxTileIndex).toInt()
////    val minY = (topLeft.y * maxTileIndex).toInt()
//    val tiles = listOf(
//        DisplayTileAndTile(
//            display = DisplayTile(
//                size = tileSize,
//                x = 0,
//                y = 0
//            ),
//            tile = Tile(
//                zoom = tileZoom,
//                x = tilex,
//                y = tiley
//            )
//        )
//    )
//    val tiles: List<DisplayTileAndTile> = buildList {
//        for (i in minX until Int.MAX_VALUE) {
//            val geoX = i.toDouble() / maxTileIndex
//            val displayX = geoXToDisplay(geoX)
//            if (displayX >= width) {
//                break
//            }
//            for (j in minY until Int.MAX_VALUE) {
//                val geoY = j.toDouble() / maxTileIndex
//                val displayY = geoYToDisplay(geoY)
//                if (displayY >= height) {
//                    break
//                }
//                add(
//                    DisplayTileAndTile(
//                        display = DisplayTile(
//                            size = tileSize,
//                            x = displayX,
//                            y = displayY
//                        ),
//                        tile = Tile(
//                            zoom = tileZoom,
//                            x = i % maxTileIndex,
//                            y = j % maxTileIndex
//                        )
//                    )
//                )
//            }
//        }
//    }
    return tiles
}
