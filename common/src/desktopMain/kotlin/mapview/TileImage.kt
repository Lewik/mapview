package mapview

import androidx.compose.ui.graphics.ImageBitmap
import mapview.TileImage

actual class TileImage(
    val platformSpecificData: ImageBitmap,
    actual val offsetX: Int = 0,
    actual val offsetY: Int = 0,
    actual val cropSize: Int = TILE_SIZE,
) {
    actual fun lightweightDuplicate(offsetX: Int, offsetY: Int, cropSize: Int): TileImage =
        mapview.TileImage(
            platformSpecificData,
            offsetX = offsetX,
            offsetY = offsetY,
            cropSize = cropSize
        )
}
