import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import mapview.*

fun main() = application {

    val focus = SchemeCoordinates(0.0, 0.0)
    val scale = 1.0

    val features = remember {
        mutableStateOf(
            listOf(
                LineFeature(
                    positionStart = SchemeCoordinates(100.0, 100.0),
                    positionEnd = SchemeCoordinates(-100.0, -100.0),
                    color = Color.Blue
                ),
                LineFeature(
                    positionStart = SchemeCoordinates(-100.0, 100.0),
                    positionEnd = SchemeCoordinates(100.0, -100.0),
                    color = Color.Green
                ),
                LineFeature(
                    positionStart = SchemeCoordinates(0.0, 0.0),
                    positionEnd = SchemeCoordinates(0.0, 50.0),
                    color = Color.Blue
                ),
                LineFeature(
                    positionStart = SchemeCoordinates(50.0, 0.0),
                    positionEnd = SchemeCoordinates(50.0, 50.0),
                    color = Color.Blue
                ),
                LineFeature(
                    positionStart = SchemeCoordinates(100.0, 0.0),
                    positionEnd = SchemeCoordinates(100.0, 50.0),
                    color = Color.Blue
                ),
                CircleFeature(
                    position = focus,
                    radius = 4.dp,
                    color = Color.Red
                ),
                CircleFeature(
                    position = SchemeCoordinates(50.0, -25.0),
                    radius = 2.dp,
                    color = Color.Black
                )
            )
        )
    }

    val viewData = remember {
        mutableStateOf(
            ViewData(
                focus = focus,
                scale = scale,
                size = Size(512f, 512f),
                showDebug = true,
            )
        )
    }



    Window(
        onCloseRequest = ::exitApplication,
        title = "Scheme View",
        state = WindowState(
            position = WindowPosition(Alignment.TopStart),
        ),
    ) {
        SchemeView(
            features = features.value,
            onViewDataChange = { TODO() },
            onResize = { viewData.value = viewData.value.copy(size = it) },
            viewData = viewData.value,
            modifier = Modifier.canvasGestures(
                viewData = viewData,
                onViewDataChange = { viewData.value = it },
                onClick = { println("CLICK as $it") }
            )
        )

        Box {
            Text("${viewData.value}")
        }

    }
}


