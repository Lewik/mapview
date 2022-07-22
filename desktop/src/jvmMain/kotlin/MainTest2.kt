import mapview.calculate
import mapview.calculateBack

fun main() {
    val tempZoom = 4
    val result = calculate(calculateBack(2, 2, tempZoom), tempZoom)
    val comparison = result == 2 to 2
    println(result)
    println(comparison)
}
