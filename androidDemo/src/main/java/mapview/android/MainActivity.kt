package mapview.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mapview.CircleFeature
import mapview.FeatureId
import mapview.SchemeCoordinates
import mapview.TextFeature
import mapview.android.mapview.tile.OpenstreetmapMapTileProvider
import mapview.view.MapView
import mapview.viewData.ViewData
import mapview.viewData.move
import mapview.viewData.multiplyScale
import mapview.viewData.resize

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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

            val features = remember {
                mutableStateOf(
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
                )
            }

            val viewData = remember {
                mutableStateOf(
                    ViewData(
                        focus = focus,
                        scale = scale,
                        size = Size(512f, 512f),
                        showDebug = true
                    )
                )
            }


            val mapTileProvider by remember {
                mutableStateOf(
                    OpenstreetmapMapTileProvider()
                )
            }

            MapView(
                mapTileProvider = mapTileProvider,
                features = features.value,
                onScroll = { amount, _ -> viewData.multiplyScale(amount) },
                onDragStart = { println("DRAG START") },
                onDrag = { viewData.move(it) },
                onClick = { offset ->
                    val coordinates = with(viewData.value) { offset.toSchemeCoordinates() }
                    println("CLICK as $coordinates")
                },
                onResize = { viewData.resize(it) },
                viewDataState = viewData,
            )
        }
    }
}



