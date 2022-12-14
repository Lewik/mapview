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
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mapview.*
import mapview.tiles.MapTileProvider
import mapview.view.MapView
import mapview.viewData.*
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import kotlin.collections.set
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


    val buildingFeatures = remember {
        derivedStateOf {
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
    }
    val lineFeatures = remember {
        derivedStateOf {
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
    }
    val features = remember {
        derivedStateOf {
            lineFeatures.value + buildingFeatures.value
        }
    }

    val focusUnderAfrica = remember {
        SchemeCoordinates(
            x = 0.0,
            y = 0.0,
        )
    }
    val focusMoscow = remember {
        SchemeCoordinates(
            x = 4187378.060833,
            y = 7508930.173748,
        )
    }
    val initialFocus = focusMoscow // SchemeCoordinates(50.0, -25.0)//focusMoscow focusUnderAfrica
    val initialScale = remember { 2.0.pow(1) }


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
            MapTileProvider(
                load = { (zoom, x, y) -> client.get("https://monitor.cr.smart-dn.ru:8899/styles/basic-dark/$zoom/$x/$y.png").readBytes().toImageBitmap() }
            )
        )
    }
    val selectHitTolerance = remember { 10.dp }
    val dragHitTolerance = remember { 5.dp }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Map View",
        state = WindowState(
//            size = DpSize(1600.dp, 1200.dp),
            position = WindowPosition(Alignment.TopStart),
        ),
    ) {
        Box {
            MapView(
                mapTileProvider = mapTileProvider,
                features = features,
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
                onFirstResize = {
                    viewData.resize(it)
                    viewData.zoomToFeatures(features.value)
                },
                viewData = viewData,
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

private fun MercatorPoint.toSchemeCoordinates() = SchemeCoordinates(x = lng, y = lat)

private fun SchemeCoordinates.toMercatorPoint() = MercatorPoint(lng = x, lat = y)
