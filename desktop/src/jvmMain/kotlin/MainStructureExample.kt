import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mapview.*
import kotlin.math.pow

fun main() = application {
    val density = LocalDensity.current

    val features = remember {
        with(density) {
            val content = object {}::class.java.getResource("/structure.json").readText()
            val structure = Json { ignoreUnknownKeys = true }.decodeFromString<Structure>(content)

            val connectionCoordinates = structure
                .buildingsById
                .values
                .flatMap { building -> building.deviceList.flatMap { it.connections.map { it to building.coordinates } } }
                .map { it.first to SchemeCoordinates(x = it.second.lng, y = it.second.lat) }
                .toMap()


            val lines = structure.linesById.values
                .map {
                    LineFeature(
                        positionStart = connectionCoordinates.getValue(it.connections[0]),
                        positionEnd = connectionCoordinates.getValue(it.connections[1]),
                        color = Color(56, 215, 41),
                        width = 2.dp,
                    )
                }


            val buildings = structure.buildingsById.values
                .filter { it.type != Building.Type.PILLAR }
                .sortedBy { it.type == Building.Type.SUB_STATION }
                .flatMap {
                    if (it.type == Building.Type.SUB_STATION) {
                        listOf(
                            CircleFeature(
                                position = SchemeCoordinates(x = it.coordinates.lng, y = it.coordinates.lat),
                                radius = 16.dp,
                                color = Color.White,
                            ),
                            CircleFeature(
                                position = SchemeCoordinates(x = it.coordinates.lng, y = it.coordinates.lat),
                                radius = 16.dp,
                                color = Color(56, 215, 41),
                                style = Stroke(width = 2.dp.toPx())
                            ),
                            CircleFeature(
                                position = SchemeCoordinates(x = it.coordinates.lng, y = it.coordinates.lat),
                                radius = 11.dp,
                                color = Color(56, 215, 41),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        )
                    } else {
                        listOf(
                            CircleFeature(
                                position = SchemeCoordinates(x = it.coordinates.lng, y = it.coordinates.lat),
                                radius = 3.dp,
                                color = Color(56, 215, 41)
                            )
                        )
                    }

                }
            lines + buildings

        }
    }

    val focusUnderAfrica = SchemeCoordinates(
        x = 0.0,
        y = 0.0,
    )

    val focusMoscow = SchemeCoordinates(
        x = 4187378.060833,
        y = 7508930.173748,
    )

    val focus = focusMoscow // SchemeCoordinates(50.0, -25.0)//focusMoscow focusUnderAfrica
    val scale = 2.0.pow(1)


    var viewData = remember {
        mutableStateOf(
            ViewData(
                focus = focus,
                scale = scale,
                size = Size(800f, 600f),
                showDebug = true,
            ).zoomToFeatures(features)
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
//                    println("KTOR request $zoom/$x/$y ($url)")
                    val result = client.get(url)
                    if (result.status.isSuccess()) {
                        val data = result.readBytes().toImageBitmap()
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
            features = features,
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


