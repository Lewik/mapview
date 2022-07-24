package mapview.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mapview.*
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


            val cache = LruCache<Triple<Int, Int, Int>, ImageBitmap>(200)
            val mapTileProvider by remember {
                val client = HttpClient(CIO)
                mutableStateOf(
                    MapTileProviderImpl(
                        getTile = { zoom, x, y ->
                            val key = Triple(zoom, x, y)
                            val cached = cache[key]
                            if (cached != null) {
                                return@MapTileProviderImpl cached
                            }
                            val url = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
                            val result = withContext(Dispatchers.IO) {
                                client.get(url)
                            }
                            if (result.status.isSuccess()) {
                                val data = result.readBytes().toImageBitmap()
                                cache.put(key, data)
                                data
                            } else {
                                println("WARNING KTOR can't get $zoom/$x/$y ")
                                null
                            }
                        },
                        minScale = 1,
                        maxScale = 19,
                    )
                )
            }

            SchemeView(
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



