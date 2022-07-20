package mapview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun SchemeViewWithGestures(
    mapTileProvider: MapTileProvider? = null,
    features: List<Feature>,
    viewPoint: ViewPoint,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    modifier: Modifier,
)
