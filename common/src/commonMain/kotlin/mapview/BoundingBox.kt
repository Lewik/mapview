package mapview

import kotlin.jvm.JvmName

class BoundingBox(val a: SchemeCoordinates, val b: SchemeCoordinates) {
    val center
        get() = SchemeCoordinates(
            x = (a.x + b.x) / 2,
            y = (a.y + b.y) / 2,
        )
}
@JvmName("schemeCoordinatesBoxToBoundingBox")
fun Iterable<SchemeCoordinates>.toBoundingBox() = BoundingBox(
    a = SchemeCoordinates(minOf { it.x }, minOf { it.y }),
    b = SchemeCoordinates(maxOf { it.x }, maxOf { it.y }),
)
@JvmName("boundingBoxToBoundingBox")
fun Iterable<BoundingBox>.toBoundingBox() = flatMap { listOf(it.a, it.b) }
    .toBoundingBox()

@JvmName("featuresToBoundingBox")
fun Iterable<Feature>.toBoundingBox() = map { it.getBoundingBox() }
    .toBoundingBox()
