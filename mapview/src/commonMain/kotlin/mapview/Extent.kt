package mapview

import kotlin.jvm.JvmName

class Extent(val a: SchemeCoordinates, val b: SchemeCoordinates) {
    val center
        get() = SchemeCoordinates(
            x = (a.x + b.x) / 2,
            y = (a.y + b.y) / 2,
        )
}

@JvmName("schemeCoordinatesBoxToExtent")
fun Iterable<SchemeCoordinates>.toExtent() = Extent(
    a = SchemeCoordinates(minOf { it.x }, minOf { it.y }),
    b = SchemeCoordinates(maxOf { it.x }, maxOf { it.y }),
)

@JvmName("ExtentToExtent")
fun Iterable<Extent>.toExtent() = flatMap { listOf(it.a, it.b) }
    .toExtent()

@JvmName("featuresToExtent")
fun Iterable<Feature>.toExtent() = map { it.getExtent() }
    .toExtent()
