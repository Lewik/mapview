package mapview

data class InternalMapState(
    val width: Int = 100, // display width in dp (pixels)
    val height: Int = 100,//display height in dp (pixels)
    val zoom: Double = 0.0,
//    val center: Meters = Meters(.0, .0),
    val topLeftOffset: DisplayPixel = DisplayPixel(.0, .0),
    val displayFeatures: List<DisplayFeature> = emptyList(),
) {
    fun getCenterOffset() = DisplayPixel(width.toDouble() / 2, height.toDouble() / 2)
}

sealed interface Feature

data class TileLayer(val url: String) : Feature
data class Line(
    val start: Meters,
    val end: Meters,
    val color: Color,
) : Feature


data class Circle(
    val center: Meters,
    val color: Color,
) : Feature

sealed interface DisplayFeature

data class DisplayTileWithImage(
    val displayTile: DisplayTile,
    val image: TileImage?,
    val tile: Tile,
) : DisplayFeature

data class DisplayLine(
    val start: DisplayPixel,
    val end: DisplayPixel,
    val color: Color,
) : DisplayFeature


data class DisplayCircle(
    val center: DisplayPixel,
    val color: Color,
) : DisplayFeature

class Color(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Int = 0xFF,
)

data class DisplayTile(
    val size: Int,//Размер на экране
    val displayPixel: DisplayPixel,//координаты на экране
)

data class DisplayTileAndTile(
    val display: DisplayTile,
    val tile: Tile,
)

//val InternalMapState.centerGeo get():GeoPt = displayToGeo(Pt(width / 2, height / 2))
//fun InternalMapState.copyAndChangeCenter(targetCenter: GeoPt): InternalMapState =
//    copy(
//        topLeft = topLeft + targetCenter - centerGeo
//    ).correctGeoXY()

/**
 * Корректируем координаты, чтобы они не выходили за край карты.
 */
//fun InternalMapState.correctGeoXY(): InternalMapState =
//    correctGeoX().correctGeoY()

//fun InternalMapState.correctGeoY(): InternalMapState {
//    val minGeoY = 0.0
//    val maxGeoY: Double = 1 - 1 / zoom
//    return if (topLeft.y < minGeoY) {
//        copy(topLeft = topLeft.copy(y = minGeoY))
//    } else if (topLeft.y > maxGeoY) {
//        copy(topLeft = topLeft.copy(y = maxGeoY))
//    } else {
//        this
//    }
//}

//fun InternalMapState.correctGeoX(): InternalMapState = copy(topLeft = topLeft.copy(x = topLeft.x.mod(1.0)))

