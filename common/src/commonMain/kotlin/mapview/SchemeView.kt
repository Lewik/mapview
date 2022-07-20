package mapview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlin.math.*


private fun IntRange.intersect(other: IntRange) = max(first, other.first)..min(last, other.last)


@Composable
fun SchemeView(
    mapTileProvider: MapTileProvider? = null,
//    computeViewPoint: (canvasSize: DpSize) -> MapViewPoint,
//    features: Map<FeatureId, MapFeature>,
//    onClick: MapViewPoint.() -> Unit,
//    config: MapViewConfig,
    features: List<Feature>,
    viewPoint: ViewPoint,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    modifier: Modifier,
) {

    var canvasSize by remember { mutableStateOf(DpSize(512.dp, 512.dp)) }

    val scale by derivedStateOf { viewPoint.scale.toFloat() }
    val floatZoom by derivedStateOf { log2(scale) }


    val zoom by derivedStateOf { floor(floatZoom).toInt() }
    val tileScale by derivedStateOf { 2f.pow(floatZoom - zoom) }


    val centerCoordinates by derivedStateOf { viewPoint.focus }

    val mapTiles = remember { mutableStateListOf<MapTile>() }
    val canvasModifier = modifier
        .fillMaxSize()

    if (mapTileProvider !== null) (
            LaunchedEffect(viewPoint, canvasSize) {
                with(mapTileProvider) {

                    val indexRange = 0 until scale.toInt()
                    val left = centerCoordinates.x - canvasSize.width.value / 2 / tileScale
                    val right = centerCoordinates.x + canvasSize.width.value / 2 / tileScale
                    val horizontalIndices: IntRange = (toIndex(left)..toIndex(right)).intersect(indexRange)

                    val top = (centerCoordinates.y + canvasSize.height.value / 2 / tileScale)
                    val bottom = (centerCoordinates.y - canvasSize.height.value / 2 / tileScale)
                    val verticalIndices: IntRange = (toIndex(bottom)..toIndex(top)).intersect(indexRange)

                    mapTiles.clear()

                    for (j in verticalIndices) {
                        for (i in horizontalIndices) {
                            val id = TileId(zoom, i, j)
//                            launch {
                            try {
                                mapTiles += loadTileAsync(id)
                            } catch (ex: Exception) {
                                if (ex !is CancellationException) {
                                    println("Failed to load tile with id=$id")
                                }
                            }
//                            }
                        }
                    }
                }
            }
            )

    Canvas(canvasModifier) {
        fun SchemeCoordinates.toOffset(): Offset = Offset(
            (canvasSize.width / 2 + (x.dp - centerCoordinates.x.dp) * tileScale).toPx(),
            (canvasSize.height / 2 + (y.dp - centerCoordinates.y.dp) * tileScale).toPx()
        )

        fun DrawScope.drawFeature(feature: Feature) {
            when (feature) {
                is CircleFeature -> drawCircle(
                    color = feature.color,
                    radius = feature.radius,
                    center = feature.position.toOffset()
                )
                is LineFeature -> drawLine(
                    color = feature.color,
                    start = feature.positionStart.toOffset(),
                    end = feature.positionEnd.toOffset()
                )
                is BitmapImageFeature -> drawImage(
                    image = feature.image,
                    topLeft = feature.position.toOffset()
                )
                is VectorImageFeature -> {
                    val offset = feature.position.toOffset()
                    translate(
                        left = offset.x - feature.size.width / 2,
                        top = offset.y - feature.size.height / 2
                    ) {
                        with(feature.painter) {
                            draw(feature.size)
                        }
                    }
                }
                is TextFeature -> TODO()
//                is TextFeature -> drawIntoCanvas { canvas ->
//                    val offset = feature.position.toOffset()
//                    canvas.nativeCanvas.drawString(
//                        feature.text,
//                        offset.x + 5,
//                        offset.y - 5,
//                        Font().apply { size = 16f },
//                        feature.color.toPaint()
//                    )
//                }
//                is CustomFeature -> drawIntoCanvas { canvas ->
//                    val offset = feature.position.toOffset()
//                    feature.drawFeature(this, offset)
//                }
            }.exhaustive()
        }

        if (canvasSize != size.toDpSize()) {
            canvasSize = size.toDpSize()
//            println("Recalculate canvas. Size: $size")
        }
        clipRect {
            if (mapTileProvider !== null) {
                val tileSize = IntSize(
                    ceil((mapTileProvider.tileSize.dp * tileScale.toFloat()).toPx()).toInt(),
                    ceil((mapTileProvider.tileSize.dp * tileScale.toFloat()).toPx()).toInt()
                )
                mapTiles.forEach { (id, image) ->
                    //converting back from tile index to screen offset
                    val offset = IntOffset(
                        (canvasSize.width / 2 + (mapTileProvider.toCoordinate(id.x).dp - centerCoordinates.x.dp) * tileScale.toFloat()).roundToPx(),
                        (canvasSize.height / 2 + (mapTileProvider.toCoordinate(id.y).dp - centerCoordinates.y.dp) * tileScale.toFloat()).roundToPx()
                    )
                    drawImage(
                        image = image,
                        dstOffset = offset,
                        dstSize = tileSize
                    )
                }
            }
            features.forEach { feature ->
                drawFeature(feature)
            }
        }
    }
}
