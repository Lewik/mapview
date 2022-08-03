import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import mapview.*
import mapview.view.SchemeView
import mapview.viewData.ViewData
import mapview.viewData.zoomToFeatures

fun main() = application {
    val density = LocalDensity.current

    val (painter, imgSize) = remember() {
        val image = useResource("img.png", ::loadImageBitmap)
        BitmapPainter(image) to DpSize(image.width.dp, image.height.dp)
    }
    val focus = SchemeCoordinates(0.0, 0.0)
    val initialScale = 1.0
    val features = remember {
        derivedStateOf {
            listOf(
                ScaledImageFeature(
                    id = FeatureId("0"),
                    position = SchemeCoordinates(.0, .0),
                    painter = painter,
                    size = imgSize
                ),
                LineFeature(
                    id = FeatureId("1"),
                    positionStart = SchemeCoordinates(100.0, 100.0),
                    positionEnd = SchemeCoordinates(-100.0, -100.0),
                    color = Color.Blue
                ),
                LineFeature(
                    id = FeatureId("2"),
                    positionStart = SchemeCoordinates(-100.0, 100.0),
                    positionEnd = SchemeCoordinates(100.0, -100.0),
                    color = Color.Green
                ),
                LineFeature(
                    id = FeatureId("3"),
                    positionStart = SchemeCoordinates(0.0, 0.0),
                    positionEnd = SchemeCoordinates(0.0, 50.0),
                    color = Color.Blue
                ),
                LineFeature(
                    id = FeatureId("4"),
                    positionStart = SchemeCoordinates(50.0, 0.0),
                    positionEnd = SchemeCoordinates(50.0, 50.0),
                    color = Color.Blue
                ),
                LineFeature(
                    id = FeatureId("5"),
                    positionStart = SchemeCoordinates(100.0, 0.0),
                    positionEnd = SchemeCoordinates(100.0, 50.0),
                    color = Color.Blue
                ),
                CircleFeature(
                    id = FeatureId("6"),
                    position = focus,
                    radius = 4.dp,
                    color = Color.Red
                ),
                CircleFeature(
                    id = FeatureId("7"),
                    position = SchemeCoordinates(50.0, -25.0),
                    radius = 2.dp,
                    color = Color.Black
                ),
                ScaledRectFeature(
                    id = FeatureId("8"),
                    position = SchemeCoordinates(50.0, -25.0),
                    size = DpSize(20.dp, 20.dp),
                    brush = SolidColor(Color.Blue)
                )
            )

        }
    }

    val viewData = remember {
        mutableStateOf(
            ViewData(
                focus = focus,
                scale = initialScale,
                showDebug = true,
                density = density,
            ).zoomToFeatures(features.value)
        )
    }



    Window(
        onCloseRequest = ::exitApplication,
        title = "Scheme View",
        state = WindowState(
            position = WindowPosition(Alignment.TopStart),
        ),
    ) {
        Box {
            SchemeView(
                features = features,
                onClick = { offset ->
                    val coordinates = with(viewData.value) { offset.toSchemeCoordinates() }
                    println("CLICK as $coordinates")
                },
                viewData = viewData,
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = { viewData.zoomToFeatures(features.value.filter { it !is ScaledImageFeature }) }) {
                    Icon(Icons.Default.Search, "")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Zoom to features")
                }
                Button(onClick = { viewData.zoomToFeatures(features.value.filter { it is ScaledImageFeature }) }) {
                    Icon(Icons.Default.Search, "")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Zoom to background")
                }
                Button(
                    onClick = { viewData.value = viewData.value.copy(showDebug = !viewData.value.showDebug) }) {
                    Icon(Icons.Default.Info, "")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = if (viewData.value.showDebug) "Hide debug" else "Show debug")
                }
            }
        }
    }
}


