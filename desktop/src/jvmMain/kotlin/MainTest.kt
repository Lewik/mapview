import mapview.SchemeCoordinates
import mapview.TileId
import mapview.calculate
import mapview.calculateBack
import kotlin.math.round

fun main() {
    val focusMoscow = SchemeCoordinates(
        x = 4187378.060833,
        y = 7508930.173748,
    )
    val datas = listOf(
        listOf(8, 154, 80) to listOf(4070119, 7357523, 4226662, 7514066),
        listOf(7, 77, 40) to listOf(4070119, 7200980, 4383205, 7514066),
        listOf(6, 38, 20) to listOf(3757033, 6887893, 4383205, 7514066),
    )

    datas.forEach {
        val (data, boundaries) = it
        val (zoom, x, y) = data
        val (b1, b2, b3, b4) = boundaries


        val actual = calculate(focusMoscow, zoom)
        val comparison = actual == TileId(zoom, x, y)
        val result = if (comparison) "OK $zoom: ${x to y}" else "WARNING BAD $zoom expect ${x to y}, actual $actual"

        if (comparison) {
            val coord = calculateBack(actual).let { round(it.x).toInt() to round(it.y).toInt() }
            val result2 = coord == b1 to b4
            val test = if (result2) "tile OK" else "tile BAD $coord ${b1 to b4}"
            println(test)
        }

        println(result)
    }

}

