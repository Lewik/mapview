package mapview

import kotlin.math.*


data class LatLon(
    val lat: Double,
    val lon: Double,
)

data class Meters(
    val x: Double,
    val y: Double,
)

data class MapPixel(
    val x: Double,
    val y: Double,
)


data class DisplayPixel(
    val x: Double,
    val y: Double,
) {
    fun move(offsetX: Double, offsetY: Double) = copy(x = x + offsetX, y = y + offsetY)
    fun plus(displayPixel: DisplayPixel) = DisplayPixel(x + displayPixel.x, y + displayPixel.y)
    fun minus(displayPixel: DisplayPixel) = DisplayPixel(x - displayPixel.x, y - displayPixel.y)
}

data class TileXY(
    val x: Int,
    val y: Int,
)

object GlobalMercator {

    //Initialize the TMS Global Mercator pyramid
    private const val originShift = 2 * PI * 6378137 / 2.0

    fun resolution(tileSize: Int = 256) = 2 * PI * 6378137 / tileSize

    //Converts given lat/lon in WGS84 Datum to XY in Spherical Mercator EPSG:900913/EPSG:3857
    fun LatLon.toMeters(): Meters {
        val mx = lon * originShift / 180.0
        var my = ln(tan((90 + lat) * PI / 360.0)) / (PI / 180.0)

        my = my * originShift / 180.0
        return Meters(mx, my)
    }

    //Converts XY point from Spherical Mercator EPSG:900913/EPSG:3857 to lat/lon in WGS84 Datum
    fun Meters.toLatLon(): LatLon {
        val lon = (x / originShift) * 180.0
        var lat = (y / originShift) * 180.0

        lat = 180 / PI * (2 * atan(exp(lat * PI / 180.0)) - PI / 2.0)
        return LatLon(lat, lon)
    }

    //Converts pixel coordinates in given zoom level of pyramid to EPSG:900913/EPSG:3857
    fun MapPixel.toMeters(tileSize: Int = 256): Meters {
        val res = resolution(tileSize)
        val x = x * res - originShift
        val y = y * res - originShift
        return Meters(x, y)
    }

    //Converts EPSG:900913/EPSG:3857 to pyramid pixel coordinates in given zoom level
//    fun Meters.toPixels(tileSize: Int = 256): MapPixel {
//        val res = resolution(tileSize)
//        val x = ((x + originShift) / res)
//        val y = ((y + originShift) / res)
//        return MapPixel(x, y)
//    }


    fun Meters.toDisplayPixel(zoom: Double, tileSize: Int = 256): DisplayPixel {
        val res = resolution(tileSize)
        val scale = zoom.zoomToScale()
        return DisplayPixel(
            x = (x + originShift) / res * scale,
            y = (y + originShift) / res * scale
        )
    }

    //Returns a tile covering region in given pixel coordinates
    fun MapPixel.toTileXY(tileSize: Int = 256): TileXY {
        val floatTileSize = tileSize.toFloat()
        val x = (ceil(x / floatTileSize) - 1).toInt()
        val y = (ceil(y / floatTileSize) - 1).toInt()
        return TileXY(x, y)
    }

    fun MapPixel.toDisplayPixel(zoom: Double): DisplayPixel {
        val scale = zoom.zoomToScaleWithinZoom()
        return DisplayPixel(
            x = (x * scale),
            y = (y * scale)
        )
    }

    fun DisplayPixel.toMapPixel(zoom: Double): MapPixel {
        val scale = zoom.zoomToScale()
        return MapPixel(
            x = (x / scale),
            y = (y / scale)
        )
    }

    fun DisplayPixel.toMeters(zoom: Double, tileSize: Int = 256): Meters {
        val scale = zoom.zoomToScale()
        val res = resolution(tileSize)
        return Meters(
            x = x / scale * res - originShift,
            y = y / scale * res - originShift
        )
    }

    fun Double.zoomToScaleWithinZoom() = 2.0.pow(this - toInt())
    fun Double.zoomToScale() = 2.0.pow(this)

    //Returns tile for given mercator coordinates
    fun Meters.toTileXY(tileSize: Int = 256): TileXY {
        val res = resolution(tileSize)
        val x1 = ((x + originShift) / res)
        val y2 = ((y + originShift) / res)
        val x = (ceil(x1 / tileSize) - 1).toInt()
        val y = (ceil(y2 / tileSize) - 1).toInt()
        return TileXY(x, y)
    }

    fun TileXY.getTopLeftMapPixel(tileSize: Int = 256) = MapPixel(
        x = (x * tileSize).toDouble(),
        y = (y * tileSize).toDouble()
    )


    fun getTiles(mapPixel: MapPixel, zoom: Int, height: Int, width: Int): List<TileXY> {
        val (tileCenterX, tileCenterY) = mapPixel.toTileXY()
        val maxTileIndex = pow2(zoom)
        val centerTile = TileXY(tileCenterX, tileCenterY)
        val (tileMinX, tileMinY) = MapPixel(mapPixel.x - width / 2, mapPixel.y - height / 2).toTileXY()
        val (tileMaxX, tileMaxY) = MapPixel(mapPixel.x + width / 2, mapPixel.y + height / 2).toTileXY()
        return (tileMinY..tileMaxY)
            .flatMap { tileY ->
                (tileMinX..tileMaxX).map { tileX ->
                    TileXY(tileX, tileY)
                }
            }
            .toSet()
            .filter {
                it.x >= 0 && it.y >= 0 && it.x < maxTileIndex && it.y < maxTileIndex
            }
            .sortedBy {
                hypot((it.x - centerTile.x).toDouble(), (it.y - centerTile.y).toDouble())
            }
    }
}
