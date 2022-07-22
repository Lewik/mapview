package mapview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size


@Composable
expect fun SchemeViewWithGestures(
    mapTileProvider: MapTileProvider? = null,
    features: State<List<Feature>>,
    viewPoint: State<ViewPoint>,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    onResize: (size: Size) -> Unit,
    modifier: Modifier,
)
