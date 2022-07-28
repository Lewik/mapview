import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import mapview.CircleFeature
import mapview.FeatureId
import mapview.SchemeCoordinates
import mapview.TextFeature
import mapview.tile.OpenstreetmapMapTileProvider
import mapview.view.MapView
import mapview.viewData.ViewData
import mapview.viewData.addScale
import mapview.viewData.move
import mapview.viewData.resize

fun main() = application {

    val focusUnderAfrica = SchemeCoordinates(
        x = 0.0,
        y = 0.0,
    )

    val focusMoscow = SchemeCoordinates(
        x = 4187378.060833,
        y = 7508930.173748,
    )

    val focus = focusMoscow
    val scale = 1.0

    val scope = rememberCoroutineScope()
    val flag = remember { mutableStateOf(true) }
//    scope.launch {
//        while (isActive) {
//            delay(500)
//            //Overwrite a feature with new color
//            flag.value = !flag.value
//        }
//    }
    val features = remember {
        derivedStateOf {
            listOf(
                CircleFeature(
                    id = FeatureId("1"),
                    position = focus,
                    radius = 3.dp,
                    color = Color.Red
                ),
                TextFeature(
                    id = FeatureId("2"),
                    position = focus,
                    text = "Test Тест",
                    color = Color.Red
                ),
            )
                //90k features
                .plus((1..300).flatMap { y ->
                    (1..300).map { x ->
                        CircleFeature(
                            id = FeatureId("generated $x-$y ${flag.value}"),
                            position = SchemeCoordinates(focus.x + x * 2, focus.y + y * 2),
                            radius = 2.dp,
                            color = listOf(
                                Color.Black,
                                Color.DarkGray,
                                Color.Gray,
                                Color.LightGray,
                                Color.White,
                                Color.Red,
                                Color.Green,
                                Color.Blue,
                                Color.Yellow,
                                Color.Cyan,
                                Color.Magenta,
                            ).random()
                        )
                    }
                }
                )
        }
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


    val mapTileProvider by remember {
        mutableStateOf(
            OpenstreetmapMapTileProvider()
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Map View",
        state = WindowState(
            position = WindowPosition(Alignment.TopStart),
        ),
    ) {
        MapView(
            mapTileProvider = mapTileProvider,
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
    }
}


