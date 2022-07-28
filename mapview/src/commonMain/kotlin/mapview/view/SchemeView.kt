package mapview.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import mapview.Feature
import mapview.viewData.ViewData


@Composable
fun SchemeView(
    features: State<List<Feature>>,
    viewData: State<ViewData>,
    onDragStart: (offset: Offset) -> Unit = {},
    onDrag: (dragAmount: Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onScroll: (scrollY: Float, target: Offset?) -> Unit = { _, _ -> },
    onClick: (offset: Offset) -> Unit = {},
    onResize: (size: Size) -> Unit,
    modifier: Modifier = Modifier,
) = AbstractView(
    features = features,
    viewData = viewData,
    onDragStart = onDragStart,
    onDrag = onDrag,
    onDragEnd = onDragEnd,
    onDragCancel = onDragCancel,
    onScroll = onScroll,
    onClick = onClick,
    onResize = onResize,
    modifier = modifier,
    tileSizeXY = IntSize.Zero,
    mapTiles = mutableStateListOf()
)

