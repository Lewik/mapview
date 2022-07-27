import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
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
import mapview.viewData.addScale
import mapview.viewData.move
import mapview.viewData.resize

fun main() = application {

    val (painter, imgSize) = remember() {
        val image = useResource("img.png", ::loadImageBitmap)
        BitmapPainter(image) to DpSize(image.width.dp, image.height.dp)
    }
    val focus = SchemeCoordinates(0.0, 0.0)
    val scale = 1.0

    val features = remember {
        mutableStateOf(
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
        SchemeView(
            features = features.value,
            onScroll = viewData::addScale,
            onDrag = viewData::move,
            onClick = { offset ->
                val coordinates = with(viewData.value) { offset.toSchemeCoordinates() }
                println("CLICK as $coordinates")
            },
            onResize = viewData::resize,
            viewDataState = viewData,
        )

        Box {
            Text("${viewData.value}")
        }

    }
}


