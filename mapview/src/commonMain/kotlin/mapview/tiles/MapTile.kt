package mapview.tiles

import androidx.compose.ui.graphics.ImageBitmap

data class MapTile(
    val id: TileId,
    val image: ImageBitmap,
    val cropSize: Int,
    val offsetX: Int,
    val offsetY: Int,
)
