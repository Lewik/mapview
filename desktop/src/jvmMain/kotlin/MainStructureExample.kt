import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
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
import mapview.viewData.*
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import kotlin.math.pow


object ScadaColor {
    val green = Color(56, 215, 41)
    val orange = Color(216, 140, 0)
}

fun main() = application {
    val density = LocalDensity.current
    val structure = remember {
        val content = object {}::class.java.getResource("/structure.json").readText()
        Json { ignoreUnknownKeys = true }.decodeFromString<Structure>(content)
    }

    val buildings = remember { structure.buildingsById.toList().toMutableStateMap() }
    val lines = remember { structure.linesById.toList().toMutableStateMap() }

    val selectedFeatureIds = remember { mutableStateOf(emptyList<FeatureId>()) }
    val draggableFeatureId = remember { mutableStateOf<FeatureId?>(null) }


    val buildingFeatures = derivedStateOf {
        with(density) {
            buildings
                .values
                .filter { it.type != Building.Type.PILLAR }
                .sortedBy { it.type == Building.Type.SUB_STATION }
                .flatMap { building ->
                    val featureId = FeatureId(building.id)
                    val selected = featureId in selectedFeatureIds.value || featureId == draggableFeatureId.value
                    val color = if (selected) ScadaColor.orange else ScadaColor.green
                    val mercator = building.coordinates
                    if (building.type == Building.Type.SUB_STATION) {
                        listOf(
                            CircleFeature(
                                id = FeatureId(building.id),
                                position = mercator.toSchemeCoordinates(),
                                radius = 16.dp,
                                color = Color.White,
                            ),
                            CircleFeature(
                                id = FeatureId(building.id + "2"),
                                position = mercator.toSchemeCoordinates(),
                                radius = 16.dp,
                                color = color,
                                style = Stroke(width = 2.dp.toPx())
                            ),
                            CircleFeature(
                                id = FeatureId(building.id + "3"),
                                position = mercator.toSchemeCoordinates(),
                                radius = 11.dp,
                                color = color,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        )
                    } else {
                        listOf(
                            CircleFeature(
                                id = FeatureId(building.id),
                                position = mercator.toSchemeCoordinates(),
                                radius = 3.dp,
                                color = color
                            )
                        )
                    }
                }
        }
    }

    val lineFeatures = derivedStateOf {
        val connectionCoordinates = buildings
            .values
            .flatMap { building -> building.deviceList.flatMap { device -> device.connections.map { it to building.coordinates } } }
            .associate { it.first to it.second.toSchemeCoordinates() }

        lines
            .values
            .map { line ->
                val featureId = FeatureId(line.id)
                val selected = featureId in selectedFeatureIds.value || featureId == draggableFeatureId.value
                val color = if (selected) ScadaColor.orange else ScadaColor.green
                val width = if (selected) 4.dp else 2.dp
                LineFeature(
                    id = featureId,
                    positionStart = connectionCoordinates.getValue(line.connections[0]),
                    positionEnd = connectionCoordinates.getValue(line.connections[1]),
                    color = color,
                    width = width,
                    cap = StrokeCap.Round,
                )
            }
    }

    val features = derivedStateOf { lineFeatures.value + buildingFeatures.value }

    val focusUnderAfrica = SchemeCoordinates(
        x = 0.0,
        y = 0.0,
    )

    val focusMoscow = SchemeCoordinates(
        x = 4187378.060833,
        y = 7508930.173748,
    )

    val initialFocus = focusMoscow // SchemeCoordinates(50.0, -25.0)//focusMoscow focusUnderAfrica
    val initialScale = 2.0.pow(1)


    val viewData = remember {
        val state = mutableStateOf(
            ViewData(
                focus = initialFocus,
                scale = initialScale,
                size = Size(800f, 600f),
                showDebug = false,
            ).zoomToFeatures(features.value)
        )
        state.value = state.value.zoomToFeatures(features.value)
        state
    }


    val cache = LruCache<Triple<Int, Int, Int>, ImageBitmap>(200)
    val mapTileProvider by remember {
        val client = HttpClient(CIO) {
            engine {
                https {
                    trustManager = object : X509TrustManager {
                        override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                        override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    }
                }
            }
        }
        mutableStateOf(
            MapTileProviderImpl(
                getTile = { zoom, x, y ->
                    val key = Triple(zoom, x, y)
                    val cached = cache[key]
                    if (cached != null) {
                        return@MapTileProviderImpl cached
                    }
                    val url = "https://monitor.cr.smart-dn.ru:8899/styles/basic-dark/$zoom/$x/$y.png"
//                    val url = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
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
    val selectHitTolerance = remember { 10.dp }
    val dragHitTolerance = remember { 5.dp }
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
            onScroll = { viewData.addScale(it) },
            onDragStart = { offset ->
                val target = getClosestFeaturesIds(
                    density = density,
                    viewData = viewData.value,
                    features = features.value.filter { it is PointFeatureType },
                    offset = offset,
                    hitTolerance = dragHitTolerance
                )
                    .firstOrNull()
                if (target != null) {
                    draggableFeatureId.value = target
                }
            },
            onDrag = { offset ->
                with(viewData.value) {
                    val target = draggableFeatureId.value
                    if (target != null) {
                        var building = buildings[target.value]
                        if (building !== null) {
                            val schemeCoordinates = building.coordinates.toSchemeCoordinates()

                            val newSchemeCoordinates = SchemeCoordinates(
                                x = schemeCoordinates.x + offset.x / scale,
                                y = schemeCoordinates.y - offset.y / scale
                            )
                            building = building.copy(coordinates = newSchemeCoordinates.toMercatorPoint())
                            buildings[target.value] = building
                        } else {
                            viewData.move(offset)
                        }
                    } else {
                        viewData.move(offset)
                    }
                }
            },
            onDragEnd = { draggableFeatureId.value = null },
            onResize = { viewData.resize(it) },
            viewDataState = viewData,
            onClick = { offset ->
                with(viewData.value) {
                    val selected = getClosestFeaturesIds(
                        density = density,
                        viewData = viewData.value,
                        features = features.value,
                        offset = offset,
                        hitTolerance = selectHitTolerance
                    )

                    selectedFeatureIds.value = selected

                    val coordinates = offset.toSchemeCoordinates()
                    println("CLICK at ($offset) $coordinates SELECTED: ${selectedFeatureIds.value}")

                }
            },
        )
    }
}

private fun MercatorPoint.toSchemeCoordinates() = SchemeCoordinates(x = lng, y = lat)

private fun SchemeCoordinates.toMercatorPoint() = MercatorPoint(lng = x, lat = y)
