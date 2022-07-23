package mapview

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset

fun Any?.exhaustive() = Unit
fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())
