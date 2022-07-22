import mapview.SchemeCoordinates
import mapview.calculate

fun main() {
    val focusMoscow = SchemeCoordinates(
        x = 4187378.060833,
        y = 7508930.173748,
    )
    val datas = listOf(
        Triple(8, 154, 80),
        Triple(7, 77, 40),
        Triple(6, 38, 20),
    )

    datas.forEach {
        val (zoom, x, y) = it
        val actual = calculate(focusMoscow, zoom)
        val result = if (actual == x to y) "OK $zoom: ${x to y}" else "WARNING BAD $zoom expect ${x to y}, actual $actual"
        println(result)
    }

}

