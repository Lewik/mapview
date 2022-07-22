@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent


@Composable
fun Outer() {
    var scale by remember { mutableStateOf(1f) }
    val modifier = Modifier
        .onPointerEvent(PointerEventType.Scroll) {
            val change = it.changes.first()
            scale -= change.scrollDelta.y

        }
        .fillMaxSize()

    Inner(modifier, Offset(50f, 50f), scale)
}

@Composable
fun Inner(modifier: Modifier, offset: Offset, scale: Float) {
    Canvas(modifier) {
        clipRect {
            drawCircle(
                color = Color.Red,
                radius = 5f,
                center = offset * scale
            )
        }
    }
}


/////

@Composable
fun OuterWithProxy() {
    var scale = remember { mutableStateOf(1f) }

    InnerProxy(Modifier, Offset(50f, 50f), scale) { scale.value = it }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InnerProxy(modifier: Modifier, offset: Offset, scale: State<Float>, onScroll: (newScale: Float) -> Unit) {
    val innerModifier = Modifier
        .onPointerEvent(PointerEventType.Scroll) {
            val change = it.changes.first()
            onScroll(scale.value - change.scrollDelta.y)
        }
        .fillMaxSize()
    Inner(innerModifier, offset, scale.value)
}
