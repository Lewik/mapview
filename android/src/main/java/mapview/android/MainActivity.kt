package mapview.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import mapview.MAPTILER_SECRET_KEY
import mapview.MapView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapView(
                modifier = Modifier.fillMaxSize(),
                mapTilerSecretKey = MAPTILER_SECRET_KEY,
//                latitude = 59.999394,
//                longitude = 29.745412,
//                startScale = 840.0,
            )
        }
    }
}
