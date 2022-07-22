import mapview.TileId
import mapview.calculate
import mapview.calculateBack

fun main() {
    val tempZoom = 4
    val tileId = TileId(tempZoom, 2, 2)
    val result = calculate(calculateBack(tileId), tempZoom)
    val comparison = result == tileId
    println(result)
    println(comparison)
}
