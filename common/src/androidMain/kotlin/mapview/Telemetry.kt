package mapview

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
internal actual fun Telemetry(state: InternalMapState) {
    Column {
        Text(state.toShortString())
    }
}
