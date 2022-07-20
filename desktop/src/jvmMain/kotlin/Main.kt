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


    val features by remember {
        mutableStateOf(
            listOf(
                CircleFeature(
                    position = SchemeCoordinates(
                        x = 50f,
                        y = 50f
                    ),
                    radius = 5f,
                    color = Color.Red
                ),
                LineFeature(
                    positionStart = SchemeCoordinates(
                        x = 10f,
                        y = 10f,
                    ),
                    positionEnd = SchemeCoordinates(
                        x = 90f,
                        y = 10f,
                    ),
                    color = Color.Blue
                )
            )
        )
    }


    var viewPoint by remember {
        mutableStateOf(
            ViewPoint(
                focus = SchemeCoordinates(
                    x = 10f,
                    y = 10f,
                ),
                scale = 1f
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
            features = features,
            onViewPointChange = { viewPoint = it },
            viewPoint = viewPoint,
            modifier = Modifier
        )
    }
}
