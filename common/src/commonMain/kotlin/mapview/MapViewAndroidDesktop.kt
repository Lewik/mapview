@file:OptIn(ExperimentalComposeUiApi::class)

package mapview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

@Composable
fun MapViewAndroidDesktop(
    modifier: Modifier,
    isInTouchMode: Boolean,
    displayFeatures: List<DisplayFeature>,
    onZoom: (DisplayPixel?, Double) -> Unit,
    onClick: (Pt) -> Unit,
    onMove: (Float, Float) -> Unit,
    onSizeUpdate: (width: Int, height: Int) -> Unit,
) {
    var previousMoveDownPos by remember { mutableStateOf<Offset?>(null) }
    var previousPressTime by remember { mutableStateOf(0L) }
    var previousPressPos by remember { mutableStateOf<Offset?>(null) }
    val density = LocalDensity.current

    fun Modifier.applyPointerInput() = pointerInput(Unit) {
        while (true) {
            val event = awaitPointerEventScope {
                awaitPointerEvent()
            }
            val change = event.changes.first()
            val current = change.position

            when (event.type) {
                PointerEventType.Scroll -> {
                    val scrollY = change.scrollDelta.y
                    if (scrollY != 0f) {
                        onZoom(DisplayPixel(current.x.toDouble(), current.y.toDouble()), -scrollY.toDouble())
                    }
                }
                PointerEventType.Move -> {
                    previousMoveDownPos = if (event.buttons.isPrimaryPressed || isInTouchMode) {
                        val previous = previousMoveDownPos
                        if (previous != null) {
                            val dx = (current.x - previous.x)
                            val dy = (current.y - previous.y)
                            if (dx.toInt() != 0 || dy.toInt() != 0) {
                                onMove(dx, dy)
                            }
                        }
                        current
                    } else {
                        null
                    }
                }
                PointerEventType.Press -> {
                    previousPressTime = timeMs()
                    previousPressPos = current
                    previousMoveDownPos = current
                }
                PointerEventType.Release -> {
                    if (!isInTouchMode) {
                        if (timeMs() - previousPressTime < Config.CLICK_DURATION_MS) {
                            val previous = previousPressPos
                            if (previous != null) {
                                if (current.distanceTo(previous) < Config.CLICK_AREA_RADIUS_PX) {
                                    onClick(current.toPt())
                                }
                            }
                        }
                    }
                    previousPressTime = timeMs()
                    previousMoveDownPos = null
                }
            }
        }
    }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        previousMoveDownPos = null
        onMove(offsetChange.x, offsetChange.y)
        onZoom(null, zoomChange.toDouble() - 1)
    }

    fun Modifier.applyTouchScreenHandlers(): Modifier {
        return transformable(
            transformableState
        ).pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    if (timeMs() - previousPressTime < Config.CLICK_DURATION_MS) {
                        val previous = previousPressPos
                        if (previous != null && previous.distanceTo(it) < Config.CLICK_AREA_RADIUS_PX) {
                            onClick(it.toPt())
                        }
                    }
                    previousPressTime = timeMs()
                    previousMoveDownPos = null
                }
            )
        }
    }

    Canvas(
        modifier.applyPointerInput()
            .run {
                if (isInTouchMode) {
                    applyTouchScreenHandlers()
                } else {
                    this
                }
            }
    ) {
        onSizeUpdate(size.width.toInt(), size.height.toInt())
        clipRect() {
            displayFeatures.forEach { feature -> //(displayTile, tileImage) ->
                when (feature) {
                    is DisplayTileWithImage -> {
                        val (displayTile, tileImage) = feature

                        if (tileImage != null) {
                            drawImage(
                                image = tileImage.extract(),
                                srcOffset = IntOffset(tileImage.offsetX, tileImage.offsetY),
                                srcSize = IntSize(tileImage.cropSize, tileImage.cropSize),
                                dstOffset = IntOffset(displayTile.displayPixel.x.toInt(), displayTile.displayPixel.y.toInt()),
                                dstSize = IntSize(displayTile.size, displayTile.size)
                            )
                        }
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(displayTile.displayPixel.x.toFloat(), displayTile.displayPixel.y.toFloat()),
                            size = Size(displayTile.size.toFloat(), displayTile.size.toFloat()),
                            style = Stroke(width = 1f)
                        )
                    }
                    is DisplayLine -> {
                        drawLine(
                            color = Color(feature.color.red, feature.color.green, feature.color.blue, feature.color.alpha),
                            start = Offset(feature.start.x.toFloat(), feature.start.y.toFloat()),
                            end = Offset(feature.end.x.toFloat(), feature.end.y.toFloat()),
                            strokeWidth = 10f
                        )
                    }
                    is DisplayCircle -> {
                        drawCircle(
                            color = Color(feature.color.red, feature.color.green, feature.color.blue, feature.color.alpha),
                            radius = 10f,
                            center = Offset(feature.center.x.toFloat(), feature.center.y.toFloat()),
                        )
                    }
                }.exhaustive()

            }
        }
    }
}

