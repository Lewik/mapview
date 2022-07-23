package mapview

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.Paint

expect fun ByteArray.toImageBitmap(): ImageBitmap
expect fun NativeCanvas.drawText1(
    string: String,
    x: Float,
    y: Float,
    fontSize: Float,
    paint: Paint,
)
