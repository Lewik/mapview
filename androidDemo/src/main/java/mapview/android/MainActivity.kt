package mapview.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import mapview.*
import mapview.tiles.MapTileProvider
import mapview.view.MapView
import mapview.viewData.ViewData
import mapview.viewData.addScale
import mapview.viewData.move
import mapview.viewData.resize

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val density = LocalDensity.current
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
//    scope.launch {
//        while (isActive) {
//            delay(500)
//            //Overwrite a feature with new color
//            flag.value = !flag.value
//        }
//    }
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
            val viewData = remember {
                mutableStateOf(
                    ViewData(
                        focus = initialFocus,
                        scale = initialScale,
                        size = Size(512f, 512f),
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

            MapView(
                mapTileProvider = mapTileProvider,
                features = features,
                onScroll = { scaleDelta, target -> viewData.addScale(scaleDelta, target) },
                onDrag = { viewData.move(it) },
                onClick = { offset ->
                    val coordinates = with(viewData.value) { offset.toSchemeCoordinates() }
                    println("CLICK as $coordinates")
                },
                onResize = { viewData.resize(it) },
                viewData = viewData,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}



