package desktopDemo

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "Map View",
        state = WindowState(
            position = WindowPosition(Alignment.TopStart),
        ),
    ) {
        val valueOuter = remember { mutableStateOf("") }
        val valueInner = remember { mutableStateOf("") }
        Column {
            TextField(
                value = valueOuter.value,
                onValueChange = { valueOuter.value = it },
                label = { Text("outer") },
            )
            TextField(
                value = valueInner.value,
                onValueChange = { valueInner.value = it },
                label = { Text("inner") },
            )
            Outer(valueOuter, valueInner)
        }
    }
}

@Composable
private fun Outer(value: State<String>, valueInner: State<String>) {
    println("SomeComposable recomposed")
    val nextState = remember { derivedStateOf { value.value + "modified outer" } }
    Text(text = nextState.value)
    Inner(value = valueInner)
}


@Composable
private fun Inner(value: State<String>) {
    println("SomeInternalComposable recomposed")
    val nextState = remember() { derivedStateOf { value.value + "modified inner" } }
    Text(text = nextState.value)
}
