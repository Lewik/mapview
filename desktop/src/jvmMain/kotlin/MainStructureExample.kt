import androidx.compose.runtime.derivedStateOf
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


object ScadaColor {
    val green = Color(56, 215, 41)
    val orange = Color(216, 140, 0)
}

fun main() = application {
    val density = LocalDensity.current

    val selectedFeatureId = remember { mutableStateOf(FeatureId("")) }

    val features = derivedStateOf {
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
                .map { line ->
                    val featureId = FeatureId(line.id)
                    val selected = selectedFeatureId.value == featureId
                    val color = if (selected) ScadaColor.orange else ScadaColor.green
                    val width = if (selected) 4.dp else 2.dp
                    LineFeature(
                        featureId = featureId,
                        positionStart = connectionCoordinates.getValue(line.connections[0]),
                        positionEnd = connectionCoordinates.getValue(line.connections[1]),
                        color = color,
                        width = width,
                    )
                }


            val buildings = structure.buildingsById.values
                .filter { it.type != Building.Type.PILLAR }
                .sortedBy { it.type == Building.Type.SUB_STATION }
                .flatMap { building ->
                    val featureId = FeatureId(building.id)
                    val selected = selectedFeatureId.value == featureId
                    val color = if (selected) ScadaColor.orange else ScadaColor.green
                    if (building.type == Building.Type.SUB_STATION) {
                        listOf(
                            CircleFeature(
                                featureId = FeatureId(building.id),
                                position = SchemeCoordinates(x = building.coordinates.lng, y = building.coordinates.lat),
                                radius = 16.dp,
                                color = Color.White,
                            ),
                            CircleFeature(
                                featureId = FeatureId(building.id + "2"),
                                position = SchemeCoordinates(x = building.coordinates.lng, y = building.coordinates.lat),
                                radius = 16.dp,
                                color = color,
                                style = Stroke(width = 2.dp.toPx())
                            ),
                            CircleFeature(
                                featureId = FeatureId(building.id + "3"),
                                position = SchemeCoordinates(x = building.coordinates.lng, y = building.coordinates.lat),
                                radius = 11.dp,
                                color = color,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        )
                    } else {
                        listOf(
                            CircleFeature(
                                featureId = FeatureId(building.id),
                                position = SchemeCoordinates(x = building.coordinates.lng, y = building.coordinates.lat),
                                radius = 3.dp,
                                color = color
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
        val state = mutableStateOf(
            ViewData(
                focus = focus,
                scale = scale,
                size = Size(800f, 600f),
                showDebug = false,
            ).zoomToFeatures(features.value)
        )
        state.value = state.value.zoomToFeatures(features.value)
        state
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
                },
                minScale = 1,
                maxScale = 19,
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
                onClick = { target ->
                    val selected = features
                        .value
                        .filter { it is PointFeatureType || it is LineFeatureType }
                        .sortedBy {
                            when (it) {
                                is PointFeatureType -> getSquaredDistance(target, it)
                                is LineFeatureType -> getSquaredDistance(target, it)
                                else -> throw IllegalStateException("Impossible")
                            }
                        }
                        .firstOrNull()
                    if (selected != null) {
                        selectedFeatureId.value = selected.featureId
                        println("SELECTED ${selected.featureId}")
                    }
                }
            )
        )
    }
}
