package mapview

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.*
import mapview.GlobalMercator.toDisplayPixel
import mapview.GlobalMercator.toMeters

data class MapViewState(
    val topLeftOffset: DisplayPixel = DisplayPixel(.0, .0),
    val zoom: Double = .0,
) {
    fun copyWithCenter(center: Meters) = copy(
        topLeftOffset = center
            .toDisplayPixel(zoom)
    )

    fun copyWithCenter(center: LatLon) = copyWithCenter(center.toMeters())
}

data class MapFeaturesState(
    val features: List<Feature>,
)

/**
 * MapView to display Earth tile maps. API provided by cloud.maptiler.com
 *
 * @param modifier to specify size strategy for this composable
 *
 * @param mapTilerSecretKey secret API key for cloud.maptiler.com
 * Here you can get this key: https://cloud.maptiler.com/maps/streets/  (register and look at url field ?key=...#)
 * For build sample projects, in file: local.properties, set key: `mapTilerSecretKey=xXxXxXxXxXxXx`
 *
 * @param latitude initial Latitude of map center.
 * Available values between [-90.0 (South) .. 90.0 (North)]
 *
 * @param longitude initial Longitude of map center
 * Available values between [-180.0 (Left) .. 180.0 (Right)]
 *
 * @param startZoom initial scale
 * (value around 1.0   = entire Earth view),
 * (value around 30.0  = Countries),
 * (value around 150.0 = Cities),
 * (value around 40000.0 = Street's)
 *
 * @param mapViewState state for Advanced usage
 * You may to configure your own state and control it.
 *
 * @param onStateChange state change handler for Advanced usage
 * You may override change state behaviour in your app
 *
 * @param onMapViewClick handle click event with point coordinates (latitude, longitude)
 * return true to enable zoom on click
 * return false to disable zoom on click
 */
@Composable
public fun MapView(
    modifier: Modifier,
    mapViewState: State<MapViewState>,
    featuresState: State<MapFeaturesState>,
    onStateChange: (MapViewState) -> Unit = { (mapViewState as? MutableState<MapViewState>)?.value = it },
    onMapViewClick: (latitude: Double, longitude: Double) -> Boolean = { lat, lon -> true },
) {
    val viewScope = rememberCoroutineScope()
    val ioScope = remember { CoroutineScope(SupervisorJob(viewScope.coroutineContext.job) + getDispatcherIO()) }
    val imageRepository = rememberTilesRepository(ioScope)

    var width: Int by remember { mutableStateOf(1000) }
    var height: Int by remember { mutableStateOf(1000) }
    val cache: MutableMap<Tile, TileImage> by remember { mutableStateOf(mutableMapOf()) }
//    println("cacheSize: ${cache.size}")
    val internalState by derivedStateOf {
        InternalMapState(
            width = width,
            height = height,
            zoom = mapViewState.value.zoom,
            topLeftOffset = mapViewState.value.topLeftOffset
        )
    }
    val displayTiles by derivedStateOf {
        val calcTiles = internalState.calcTiles()
        val tilesToDisplay = mutableListOf<DisplayTileWithImage>()
        val tilesToLoad = mutableSetOf<Tile>()
        calcTiles.forEach {
            val cachedImage = cache[it.tile]
            if (cachedImage != null) {
                tilesToDisplay.add(DisplayTileWithImage(it.display, cachedImage, it.tile))
            } else {
                tilesToLoad.add(it.tile)
//                val croppedImage = cache.searchOrCrop(it.tile)
                tilesToDisplay.add(DisplayTileWithImage(it.display, null, it.tile))
            }
        }
        viewScope.launch {
            tilesToLoad.forEach { tile ->
                try {
                    val image: TileImage = imageRepository.loadContent(tile)
                    cache += (tile to image)
                    println("Loaded $tile into the cache")
                } catch (t: Throwable) {
                    println("exception in tiles loading, throwable: $t")
                    // ignore errors. Tile image loaded with retries
                }
            }
        }
        tilesToDisplay
    }

//    val pixelCenter = internalState.center.toPixels()
//    val zoomedCenter = pixelCenter.toZoomedMapPixel(internalState.zoom)
//    val offsetX = internalState.topLeftOffset.x
//    val offsetY = internalState.topLeftOffset.y


    val features: List<DisplayFeature> by derivedStateOf {
        val features = mutableListOf<DisplayFeature>()
        featuresState.value.features
            .forEach { feature ->
                when (feature) {
                    is TileLayer -> features.addAll(displayTiles)
                    is Line -> DisplayLine(
                        start = feature.start.toDisplayPixel(internalState.zoom).plus(internalState.topLeftOffset),
                        end = feature.end.toDisplayPixel(internalState.zoom).plus(internalState.topLeftOffset),
                        color = feature.color
                    ).also { features.add(it) }
                    is Circle -> {
                        val toDisplayPixel = feature.center.toDisplayPixel(internalState.zoom)
                        DisplayCircle(
                            center = toDisplayPixel.plus(internalState.topLeftOffset),
                            color = feature.color
                        ).also {
                            features.add(it)
                            //                        println("internalState.offset ${internalState.offset} circle: ${it.center}")
                        }
                    }
                }
            }
        features
    }

//    println("features: ${features.size}")

    MapViewAndroidDesktop(
        modifier = modifier,
        isInTouchMode = false,
        displayFeatures = features,
        onZoom = { p: DisplayPixel?, change ->
            println("onZoom")
            onStateChange(internalState.zoom(p, change).toExternalState())
        },
        onClick = {
            println("onClick")
//            if (onMapViewClick(internalState.displayToGeo(it).latitude, internalState.displayToGeo(it).longitude)) {
//                onStateChange(internalState.zoom(it, Config.ZOOM_ON_CLICK).toExternalState())
//            }
        },
        onMove = { dx, dy ->
//            println("onMove")
//            val center = internalState
//                .center
//                .toPixels()
//                .toZoomedMapPixel(internalState.zoom)
//                .let {
//                    it.copy(
//                        x = it.x - dx,
//                        y = it.y - dy
//                    )
//                }
//                .toMapPixel(internalState.zoom)
//                .toMeters()
            val offset = internalState.topLeftOffset.copy(
                x = internalState.topLeftOffset.x + dx,
                y = internalState.topLeftOffset.y + dy,
            )
            onStateChange(internalState.copy(topLeftOffset = offset).toExternalState())
        },
        onSizeUpdate = { w, h ->
            println("onSizeUpdate")
            width = w
            height = h
            onStateChange(internalState.copy(width = w, height = h).toExternalState())
        }
    )
    if (Config.DISPLAY_TELEMETRY) {
        Telemetry(internalState)
    }
}

//expect interface DisplayModifier

fun InternalMapState.toExternalState() = MapViewState(
    topLeftOffset = topLeftOffset,
    zoom = zoom,
)
