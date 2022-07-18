package mapview

class BoundingBox(val a: SchemeCoordinates, val b: SchemeCoordinates) {
    val center
        get() = SchemeCoordinates(
            x = (a.x + b.x) / 2,
            y = (a.y + b.y) / 2,
        )
}

fun Iterable<SchemeCoordinates>.toBoundingBox() = BoundingBox(
    a = SchemeCoordinates(minOf { it.x }, minOf { it.y }),
    b = SchemeCoordinates(maxOf { it.x }, maxOf { it.y }),
)

fun Iterable<BoundingBox>.toBoundingBox() = flatMap { listOf(it.a, it.b) }
    .toBoundingBox()

fun Iterable<Feature>.toBoundingBox() = map { it.getBoundingBox() }
    .toBoundingBox()
