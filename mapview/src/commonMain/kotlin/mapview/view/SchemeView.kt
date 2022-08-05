package mapview.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import mapview.Feature
import mapview.viewData.ViewData
import mapview.viewData.addScale
import mapview.viewData.move
import mapview.viewData.resize


@Composable
fun SchemeView(
    features: State<List<Feature>>,
    viewData: MutableState<ViewData>,
    onDragStart: (offset: Offset) -> Unit = {},
    onDrag: (dragAmount: Offset) -> Unit = { viewData.move(it) },
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onScroll: (scrollY: Float, target: Offset?) -> Unit = { scaleDelta, target ->
        viewData.addScale(scaleDelta, target)
    },
    onClick: (offset: Offset) -> Unit = {},
    onResize: (size: Size) -> Unit = { viewData.resize(it) },
    onFirstResize: (size: Size) -> Unit = { onResize(it) },
    //size should be specified: see Canvas
    modifier: Modifier,
) = AbstractView(
    features = features,
    viewData = viewData,
    onDragStart = onDragStart,
    onDrag = onDrag,
    onDragEnd = onDragEnd,
    onDragCancel = onDragCancel,
    onScroll = onScroll,
    onClick = onClick,
    onFirstResize = onFirstResize,
    onResize = onResize,
    modifier = modifier,
    tileSizeXY = IntSize.Zero,
    mapTiles = mutableStateListOf()
)

