package mapview.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mapview.*

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
                            position = focus,
                            radius = 3f,
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
                        size = Size(512f, 512f)
                    )
                )
            }


            val cache = LruCache<Triple<Int, Int, Int>, ByteArray>(50)
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
                                val data = result.readBytes()
                                cache.put(key, data)
                                data
                            } else {
                                println("WARNING KTOR can't get $zoom/$x/$y ")
                                null
                            }
                        }
                    )
                )
            }

            SchemeView(
                mapTileProvider = mapTileProvider,
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
}



