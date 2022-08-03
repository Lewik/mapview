package mapview.viewData

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import mapview.Extent
import mapview.Feature

fun MutableState<ViewData>.move(dragAmount: Offset) {
    value = value.move(dragAmount)
}

fun MutableState<ViewData>.resize(newSize: Size) {
    value = value.resize(newSize)
}

fun MutableState<ViewData>.addScale(
    scaleDelta: Float,
    target: Offset? = null,
) {
    value = value.addScale(
        scaleDelta = scaleDelta,
        target = target
    )
}

fun MutableState<ViewData>.multiplyScale(multiplier: Float) {
    value = value.multiplyScale(multiplier)
}

fun MutableState<ViewData>.zoomToExtent(
    extent: Extent,
    padding: Dp = ViewData.PaddingDefault,
) {
    value = value.zoomToExtent(
        extent = extent,
        padding = padding
    )
}

fun MutableState<ViewData>.zoomToFeatures(
    features: Iterable<Feature>,
    padding: Dp = ViewData.PaddingDefault,
) {
    value = value.zoomToFeatures(features, padding)
}
