// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import mapview.*

fun main() = application {

//    val content = object {}::class.java.getResource("/structure.json").readText()
//    val structure = Json { ignoreUnknownKeys = true }.decodeFromString<Structure>(content)
//
//    val connectionCoordinates = structure
//        .buildingsById
//        .values
//        .flatMap { building -> building.deviceList.flatMap { it.connections.map { it to building.coordinates } } }
//        .map { it.first to Meters(x = it.second.lat, y = it.second.lng) }
//        .toMap()
//
//
//    val lines = structure.linesById.values
//        .map {
//            Line(
//                start = connectionCoordinates.getValue(it.connections[0]),
//                end = connectionCoordinates.getValue(it.connections[1]),
//                color = Color(0, 255, 0)
//            )
//        }
//
//    val buildings = structure.buildingsById.values
//        .map {
//            Circle(
//                center = Meters(x = it.coordinates.lat, y = it.coordinates.lng),
//                color = Color(0, 255, 0)
//            )
//        }


    val focusUnderAfrica = SchemeCoordinates(
        x = 0.0,
        y = 0.0,
    )

    val focusMoscow = SchemeCoordinates(
        x = 4187378.060833,
        y = 7508930.173748,
    )

    val focus = focusUnderAfrica
    val scale = 5.0

    val features by remember {
        mutableStateOf(
            listOf(
                CircleFeature(
                    position = focus,
                    radius = 5f,
                    color = Color.Red
                ),
//                LineFeature(
//                    positionStart = SchemeCoordinates(
//                        x = 10.0,
//                        y = 10.0,
//                    ),
//                    positionEnd = SchemeCoordinates(
//                        x = 90.0,
//                        y = 10.0,
//                    ),
//                    color = Color.Blue
//                )
            )
        )
    }

    var viewPoint by remember {
        mutableStateOf(
            ViewPoint(
                focus = focus,
                scale = scale
            )
        )
    }


    val mapTileProvider by remember {
        val client = HttpClient(CIO)
        mutableStateOf(
            MapTileProviderImpl(
                getTile = { zoom, x, y ->
                    println("KTOR request $zoom/$x/$y")
                    val url = "https://tile.openstreetmap.org/$zoom/$x/$y.png"
                    client.get(url).readBytes()
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
        SchemeViewWithGestures(
            mapTileProvider = mapTileProvider,
            features = features,
            onViewPointChange = { viewPoint = it },
            viewPoint = viewPoint,
            modifier = Modifier
        )
    }
}
