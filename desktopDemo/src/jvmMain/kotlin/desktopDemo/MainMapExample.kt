package desktopDemo

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mapview.*
import mapview.tiles.MapTileProvider
import mapview.view.MapView
import mapview.viewData.ViewData
import mapview.viewData.zoomToFeatures

fun main() = application {

    val focusUnderAfrica = SchemeCoordinates(
        x = 0.0,
        y = 0.0,
    )

    val focusMoscow = SchemeCoordinates(
        x = 4187378.060833,
        y = 7508930.173748,
    )

    val initialFocus = focusMoscow
    val initialScale = 1.0


    val flag = remember { mutableStateOf(true) }

    if (false) {
        val scope = rememberCoroutineScope()
        scope.launch {
            while (isActive) {
                delay(500)
                //Overwrite a feature with new color
                flag.value = !flag.value
            }
        }
    }

    val features = remember {
        derivedStateOf {
            //90k features

            (1..300).flatMap { y ->
                (1..300).map { x ->
                    CircleFeature(
                        id = FeatureId("generated $x-$y ${flag.value}"),
                        position = SchemeCoordinates(initialFocus.x + x * 2, initialFocus.y + y * 2),
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
                .plus(
                    listOf(
                        CircleFeature(
                            id = FeatureId("1"),
                            position = initialFocus,
                            radius = 3.dp,
                            color = Color.Red
                        ),
                        TextFeature(
                            id = FeatureId("2"),
                            position = initialFocus,
                            text = "Big test",
                            color = Color.Black,
                            fontSize = 64.dp
                        ),
                        TextFeature(
                            id = FeatureId("2"),
                            position = initialFocus,
                            text = "Test Тест",
                            color = Color.Red,
                            fontSize = 16.dp
                        ),
                    )
                )

        }
    }

    val density = LocalDensity.current
    val viewData = remember {
        mutableStateOf(
            ViewData(
                focus = initialFocus,
                scale = initialScale,
                showDebug = true,
                density = density,
            )
        )
    }


    val mapTileProvider = remember {
        val client = HttpClient()
        mutableStateOf(
            MapTileProvider(
                parallel = 1,
                load = { client.get(MapTileProvider.osmUrl(it).also { println(it) }).readBytes().toImageBitmap() }
            )
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Map View",
        state = WindowState(
            position = WindowPosition(Alignment.TopStart),
        ),
    ) {
        Box {
            MapView(
                mapTileProvider = mapTileProvider,
                features = features,
                onClick = { offset ->
                    val coordinates = with(viewData.value) { offset.toSchemeCoordinates() }
                    println("CLICK as $coordinates")
                },
                viewData = viewData,
                modifier = Modifier.fillMaxSize(),
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = { viewData.zoomToFeatures(features.value) }) {
                    Icon(Icons.Default.Search, "")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Zoom to features")
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


