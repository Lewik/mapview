// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import kotlin.math.pow

@OptIn(ExperimentalComposeUiApi::class)
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

    val focus = focusMoscow // SchemeCoordinates(50.0, -25.0)//focusMoscow focusUnderAfrica
    val scale = 2.0.pow(1)

    val features = remember {
        mutableStateOf(
            listOf(
                LineFeature(
                    positionStart = SchemeCoordinates(100.0, 100.0),
                    positionEnd = SchemeCoordinates(-100.0, -100.0),
                    color = Color.Blue
                ),
                LineFeature(
                    positionStart = SchemeCoordinates(-100.0, 100.0),
                    positionEnd = SchemeCoordinates(100.0, -100.0),
                    color = Color.Green
                ),
                LineFeature(
                    positionStart = SchemeCoordinates(0.0, 0.0),
                    positionEnd = SchemeCoordinates(0.0, 50.0),
                    color = Color.Blue
                ),
                LineFeature(
                    positionStart = SchemeCoordinates(50.0, 0.0),
                    positionEnd = SchemeCoordinates(50.0, 50.0),
                    color = Color.Blue
                ),
                LineFeature(
                    positionStart = SchemeCoordinates(100.0, 0.0),
                    positionEnd = SchemeCoordinates(100.0, 50.0),
                    color = Color.Blue
                ),
                CircleFeature(
                    position = focus,
                    radius = 3f,
                    color = Color.Red
                ),
                CircleFeature(
                    position = SchemeCoordinates(50.0, -25.0),
                    radius = 2f,
                    color = Color.Black
                )
            )
        )
    }

    var viewPoint = remember {
        mutableStateOf(
            ViewPoint(
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
//                    println("KTOR request $zoom/$x/$y ($url)")
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

//        SchemeViewWithGestures(
//            mapTileProvider = mapTileProvider,
//            features = features,
//            onViewPointChange = { viewPoint.value = it },
//            viewPoint = viewPoint,
//            modifier = Modifier
//        )
        Box {
            Text("${viewPoint.value}")
        }
        SchemeView(
            mapTileProvider = mapTileProvider,
            features = features.value,
            onViewPointChange = { TODO() },
            onResize = { viewPoint.value = viewPoint.value.copy(size = it) },
            viewPoint = viewPoint.value,
            modifier = Modifier.canvasGestures(
                viewPoint = viewPoint,
                onViewPointChange = { viewPoint.value = it },
                onClick = { println("CLICK as $it") }
            )
        )

//        Outer()
//        OuterWithProxy()
    }
}


