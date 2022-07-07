//package mapview
//
//
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//
//
///**
// * Workaround для склеивания MapViewAndroidDesktop и MapViewBrowser между модулями.
// * Если бы Я настроил Hierarchical Multiplatform sourceSet-ы, то такой workaround бы не потребовался
// */
//@Composable
//internal expect fun PlatformMapView(
//    modifier: Modifier,
//    tiles: List<DisplayTileWithImage>,
//    onZoom: (Pt?, Double) -> Unit,
//    onClick: (Pt) -> Unit,
//    onMove: (Int, Int) -> Unit,
//    onSizeUpdate: (width: Int, height: Int) -> Unit
//)
