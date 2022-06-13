package mapview

import androidx.compose.ui.graphics.ImageBitmap

expect fun ByteArray.toImageBitmap(): ImageBitmap
expect fun TileImage.extract():ImageBitmap
