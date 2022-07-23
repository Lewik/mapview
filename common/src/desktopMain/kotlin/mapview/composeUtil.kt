package mapview

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Font
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine

actual fun ByteArray.toImageBitmap(): ImageBitmap = Image.makeFromEncoded(this).toComposeImageBitmap()


actual fun NativeCanvas.drawText1(
    string: String,
    x: Float,
    y: Float,
    fontSize: Float,
    paint: androidx.compose.ui.graphics.Paint,
) {

    val textLine = TextLine.make(string, Font().apply { size = 16f })
    drawTextLine(
        line = textLine,
        x = x,
        y = y,
        paint = Paint().apply { this.color = color }
    )
}
