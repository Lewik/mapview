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
    a = SchemeCoordinates(minOfOrNull { it.x } ?: .0, minOfOrNull { it.y } ?: .0),
    b = SchemeCoordinates(maxOfOrNull { it.x } ?: .0, maxOfOrNull { it.y } ?: .0),
)

@JvmName("ExtentToExtent")
fun Iterable<Extent>.toExtent() = flatMap { listOf(it.a, it.b) }
    .toExtent()

@JvmName("featuresToExtent")
fun Iterable<Feature>.toExtent() = map { it.getExtent() }
    .toExtent()
