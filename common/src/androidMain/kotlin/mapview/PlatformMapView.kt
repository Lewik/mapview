package mapview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual typealias DisplayModifier = Modifier

@Composable
internal actual fun PlatformMapView(
    modifier: DisplayModifier,
    tiles: List<DisplayTileWithImage<TileImage>>,
    onZoom: (Pt?, Double) -> Unit,
    onClick: (Pt) -> Unit,
    onMove: (Int, Int) -> Unit,
    onSizeUpdate: (width: Int, height: Int) -> Unit
) {
    MapViewAndroidDesktop(
        modifier = Modifier.fillMaxSize(),
        isInTouchMode = true,
        tiles = tiles,
        onZoom = onZoom,
        onClick = onClick,
        onMove = onMove,
        onSizeUpdate = onSizeUpdate,
    )
}

