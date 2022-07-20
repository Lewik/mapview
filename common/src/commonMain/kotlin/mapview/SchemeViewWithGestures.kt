package mapview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
expect fun SchemeViewWithGestures(
    features: List<Feature>,
    viewPoint: ViewPoint,
    onViewPointChange: (viewPoint: ViewPoint) -> Unit,
    modifier: Modifier,
)
