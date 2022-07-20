package mapview.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import mapview.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val features by remember {
                mutableStateOf(
                    listOf(
                        CircleFeature(
                            position = SchemeCoordinates(
                                x = 50f,
                                y = 50f
                            ),
                            radius = 5f,
                            color = Color.Red
                        ),
                        LineFeature(
                            positionStart = SchemeCoordinates(
                                x = 10f,
                                y = 10f,
                            ),
                            positionEnd = SchemeCoordinates(
                                x = 90f,
                                y = 10f,
                            ),
                            color = Color.Blue
                        )
                    )
                )
            }


            var viewPoint by remember {
                mutableStateOf(
                    ViewPoint(
                        focus = SchemeCoordinates(
                            x = 10f,
                            y = 10f,
                        ),
                        scale = 1f
                    )
                )
            }



            SchemeViewWithGestures(
                features = features,
                onViewPointChange = { viewPoint = it },
                viewPoint = viewPoint,
                modifier = Modifier
            )


        }
    }
}



