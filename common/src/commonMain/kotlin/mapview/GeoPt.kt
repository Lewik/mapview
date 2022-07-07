package mapview

import kotlin.math.*

/**
 * Точка в относительных координатах на карте
 * x меняется от 0 до 1  (что соответствует longitude -180 .. 180)
 * y меняется от 0 до 1  (что соответствует latitude 90 .. -90)
 */
//data class GeoPt(val x: Double, val y: Double)



//private val const = 20037508.34

data class MercatorPoint(
    val x: Double,
    val y: Double,
) {
//    fun toLonLat(): GeoPointDegree {
//        val lon = (x / const) * 180
//        var lat = (y / const) * 180
//        lat = 180 / PI * (2 * atan(exp(lat * PI / 180)) - PI / 2)
//        return GeoPointDegree(lon, lat)
//    }
}

data class GeoPointDegree(
    val lng: Double,
    val lat: Double,
) {
//    fun toMercator(): MercatorPoint {
//        val x = lng * const / 180
//        var y = ln(tan((90 + lat) * PI / 360)) / (PI / 180)
//        y = y * const / 180
//
//        return MercatorPoint(x, y)
//    }
}


/**
 * Точка в экранных координатак (также может представлять себя как расстояние между точками)
 */
data class Pt(val x: Int, val y: Int)

//val GeoPt.longitude get():Double = x * 360.0 - 180.0
//val GeoPt.latitude
//    get():Double {
//        val latRad = atan(sinh(PI * (1 - 2 * y)))
//        return latRad / PI * 180.0
//    }

//fun createGeoPt(latitude: Double, longitude: Double): GeoPt {
//    val x = (longitude + 180) / 360
//    val y = (1 - ln(tan(latitude.toRad()) + 1 / cos(latitude.toRad())) / PI) / 2
//    return GeoPt(x, y)
//}

//fun Double.toRad() = this * PI / 180

//operator fun Pt.minus(other: Pt): Pt = Pt(this.x - other.x, this.y - other.y)
//fun Pt.distanceTo(other: Pt): Double {
//    val dx = other.x - x
//    val dy = other.y - y
//    return sqrt(dx * dx.toDouble() + dy * dy.toDouble())
//}

//operator fun GeoPt.minus(minus: GeoPt): GeoPt {
//    return GeoPt(x - minus.x, y - minus.y)
//}

//operator fun GeoPt.plus(other: GeoPt): GeoPt {
//    return GeoPt(x + other.x, y + other.y)
//}
