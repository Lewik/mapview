package mapview.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mapview.TextFeature
import mapview.drawText1
import mapview.viewData.ViewData

@Composable
fun DrawTextFeatures(
    features: ImmutableList<TextFeature>,
    viewData: ViewData,
    asyncRenderThreshold: Double,
) {
    with(viewData) {
        with(LocalDensity.current) {
            fun DpOffset.toOffset() = Offset(x.toPx(), y.toPx())
            fun draw(features: ImmutableList<TextFeature>, canvas: Canvas) {
                features.list.forEach { feature ->
                    val offset = feature.position.toOffset() - feature.centerOffset.toOffset()
                    if (isTextVisible(offset)) {
                        canvas.nativeCanvas.drawText1(
                            string = feature.text,
                            x = offset.x,
                            y = offset.y,
                            fontSize = feature.fontSize.toPx(),
                            paint = Paint().apply { color = feature.color }
                        )
                    }
                }
            }


            if (scale < asyncRenderThreshold) {
                val image: MutableState<ImageBitmap?> = remember(viewData, features) { mutableStateOf(null) }
                LaunchedEffect(features, viewData) {
                    withContext(Dispatchers.IO) {
                        val imageBitmap = ImageBitmap(viewData.size.width.toInt(), viewData.size.height.toInt())
                        val canvas = Canvas(imageBitmap)
                        draw(features, canvas)
                        image.value = imageBitmap
                    }
                }
                Canvas(Modifier) {
                    image.value?.let { loadedImage ->
                        drawImage(loadedImage)
                    }
                }
            } else {
                Canvas(
                    Modifier.width(viewData.size.width.toDp()).height(viewData.size.height.toDp())
                ) {
                    clipRect {
                        drawIntoCanvas { canvas ->
                            draw(features, canvas)
                        }
                    }
                }
            }
        }
    }
}


