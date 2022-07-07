// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mapview.*
import mapview.Line


fun main() = application {

    val content = object {}::class.java.getResource("/structure.json").readText()
    val structure = Json { ignoreUnknownKeys = true }.decodeFromString<Structure>(content)

    val connectionCoordinates = structure
        .buildingsById
        .values
        .flatMap { building -> building.deviceList.flatMap { it.connections.map { it to building.coordinates } } }
        .map { it.first to Meters(x = it.second.lat, y = it.second.lng) }
        .toMap()


    val lines = structure.linesById.values
        .map {
            Line(
                start = connectionCoordinates.getValue(it.connections[0]),
                end = connectionCoordinates.getValue(it.connections[1]),
                color = Color(0, 255, 0)
            )
        }

    val buildings = structure.buildingsById.values
        .map {
            Circle(
                center = Meters(x = it.coordinates.lat, y = it.coordinates.lng),
                color = Color(0, 255, 0)
            )
        }
        .take(1)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Map View",
        state = WindowState(
            position = WindowPosition(Alignment.TopStart),
            size = getPreferredWindowSize(1200, 600)
        ),
    ) {
        val mapViewState = remember {
            mutableStateOf(
                MapViewState(zoom = .0)
                    .copyWithCenter(LatLon(59.999394, 29.745412))
            )
        }

        val featuresState = remember {
            mutableStateOf(
                MapFeaturesState(
                    listOf(TileLayer("someUrl{x}{y}"))
                        .plus(lines)
                        .plus(buildings)
                )
            )
        }
        MapView(
            modifier = Modifier.fillMaxSize(),
            mapViewState = mapViewState,
            featuresState = featuresState
        ) { latitude, longitude ->
            println("click on geo coordinates: (lat $latitude, lon $longitude)")
            true
        }
//        val animate = false
//        if (animate) {
//            AnimatedMapView()
//        } else {
//            MapView(
//                modifier = Modifier.fillMaxSize(),
//                mapTilerSecretKey = MAPTILER_SECRET_KEY,
//                latitude = 59.999394,
//                longitude = 29.745412,
//                startScale = 1.0,
//                onMapViewClick = { latitude, longitude ->
//                    println("click on geo coordinates: (lat $latitude, lon $longitude)")
//                    true
//                }
//            )
//        }
    }
}


@Composable
fun AnimatedMapView() {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedScale: Float by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 4200f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5_000
                2f at 500
                100f at 2000
                4100f at 4_500
            },
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedMapViewState = derivedStateOf {
        MapViewState(zoom = animatedScale.toDouble())
            .copyWithCenter(LatLon(59.999394, 29.745412))
    }
    val featuresState = remember {
        mutableStateOf(
            MapFeaturesState(
                listOf(
                    Line(
                        start = Meters(.0, .0),
                        end = Meters(100000.0, 100000.0),
                        color = Color(0, 0, 0, 0)
                    )
                )
            )
        )
    }

    MapView(
        modifier = Modifier.fillMaxSize(),
        mapViewState = animatedMapViewState,
        onStateChange = {},
        featuresState = featuresState
    )
}
