import androidx.compose.runtime.getValue
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
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import mapview.*

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

    val features = remember {
        mutableStateOf(
            listOf(
                CircleFeature(
                    position = focus,
                    radius = 3.dp,
                    color = Color.Red
                ),
                TextFeature(
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
                showDebug = true,
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
                    val result = client.get(url)
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

    Window(
        onCloseRequest = ::exitApplication,
        title = "Map View",
        state = WindowState(
            position = WindowPosition(Alignment.TopStart),
        ),
    ) {
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
    }
}


