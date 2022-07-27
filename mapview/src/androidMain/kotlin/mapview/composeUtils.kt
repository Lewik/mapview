package mapview

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asImageBitmap

actual fun ByteArray.toImageBitmap(): ImageBitmap = BitmapFactory
    .decodeByteArray(this, 0, size)
    .asImageBitmap()

actual fun NativeCanvas.drawText1(
    string: String,
    x: Float,
    y: Float,
//    font: Font?,
    fontSize: Float,
    paint: Paint,
) = drawText(
    string,
    x,
    y,
    paint.asFrameworkPaint()
)
