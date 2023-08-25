package com.example.plotlineassignment.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.plotlineassignment.TooltipPopupProperties
import com.example.plotlineassignment.ui.theme.DefaultDark
import kotlin.math.absoluteValue

@Composable
fun TooltipPopup(
    modifier: Modifier = Modifier,
    tooltipPopupProperties: TooltipPopupProperties,
    anchor: @Composable (tooltipState: TooltipState) -> Unit,
    tooltipState: TooltipState,
//    alignment: TooltipAlignment
) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current

    var tooltipContentSize by remember {
        mutableStateOf(Pair(0.dp, 0.dp))
    }
    var offset by remember {
        mutableStateOf(IntOffset(0, 0))
    }
    var anchorSpaceStart by remember {
        mutableFloatStateOf(0f)
    }
    var anchorSpaceEnd by remember {
        mutableFloatStateOf(0f)
    }
    var anchorSpaceTop by remember {
        mutableFloatStateOf(0f)
    }
    var anchorSpaceBottom by remember {
        mutableFloatStateOf(0f)
    }

    val screenWidth = remember {
        with(density) { config.screenWidthDp.dp.toPx() }
    }

    var arrowAlignment by remember {
        mutableStateOf(TooltipAlignment.TOP)
    }

    Box(modifier = modifier) {
        Box(modifier = Modifier
            .onGloballyPositioned { coordinates ->
                val buttonSize = coordinates.size
                anchorSpaceStart = coordinates.positionInRoot().x
                anchorSpaceEnd =
                    (screenWidth.dp.value - (anchorSpaceStart + buttonSize.width)).absoluteValue
                Log.d(
                    "alignment-end",
                    "button size: ${coordinates.size.width} space end: $anchorSpaceEnd space-start: $anchorSpaceStart"
                )
            }) {
            anchor(tooltipState)
        }
        if (tooltipState.isVisible) {
            Popup(
                onDismissRequest = { tooltipState.toggleVisibility() },
                properties = PopupProperties(dismissOnClickOutside = true),
                alignment = when (tooltipPopupProperties.alignment) {
                    TooltipAlignment.TOP -> {
                        arrowAlignment = TooltipAlignment.BOTTOM
                        offset = IntOffset(x = 0, y = -tooltipContentSize.first.value.toInt())
                        Alignment.TopCenter
                    }

                    TooltipAlignment.BOTTOM -> {
                        arrowAlignment = TooltipAlignment.TOP
                        offset = IntOffset(x = 0, y = tooltipContentSize.first.value.toInt())
                        Alignment.BottomCenter
                    }

                    TooltipAlignment.START -> {
                        if (anchorSpaceStart >= tooltipContentSize.second.value) {
                            arrowAlignment = TooltipAlignment.END
                            offset = IntOffset(x = -tooltipContentSize.second.value.toInt(), y = 0)
                            Alignment.CenterStart
                        }
                        // If there is not enough space between anchor's start and the screen, try to show the tooltip on the anchor's end
                        else {
                            arrowAlignment = TooltipAlignment.START
                            offset = IntOffset(x = tooltipContentSize.second.value.toInt(), y = 0)
                            Alignment.CenterEnd
                        }
                    }

                    TooltipAlignment.END -> {
                        if (anchorSpaceEnd >= tooltipContentSize.second.value) {
                            Log.d("alignment-end", "if block")
                            arrowAlignment = TooltipAlignment.START
                            offset = IntOffset(x = tooltipContentSize.second.value.toInt(), y = 0)
                            Alignment.CenterEnd
                        }
                        // If there is not enough space between anchor's end and the screen, try to show the tooltip on the anchor's start
                        else {
                            Log.d("alignment-end", "else block")
                            arrowAlignment = TooltipAlignment.END
                            offset = IntOffset(x = -tooltipContentSize.second.value.toInt(), y = 0)
                            Alignment.CenterStart
                        }
                    }
                },
                offset = offset
            ) {
                Tooltip(
                    text = tooltipPopupProperties.tooltipText,
                    textSize = tooltipPopupProperties.textSize,
                    textColor = tooltipPopupProperties.textColor,
                    backgroundColor = tooltipPopupProperties.backgroundColor,
                    cornerRadius = tooltipPopupProperties.cornerRadius,
                    arrowHeight = tooltipPopupProperties.arrowHeight,
                    arrowWidth = tooltipPopupProperties.arrowWidth,
                    arrowAlignment = arrowAlignment,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val height = coordinates.size.height.dp
                        val width = coordinates.size.width.dp
                        tooltipContentSize = height to width

                    })
            }
        }
    }
}

@Preview
@Composable
fun TooltipDemo() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        TooltipPopup(
            tooltipState = rememberTooltipState(initiallyTooltipVisible = false),
            tooltipPopupProperties = TooltipPopupProperties(
                textColor = Color.White,
                backgroundColor = DefaultDark,
                cornerRadius = 4.dp,
                arrowHeight = 10.dp,
                arrowWidth = 10.dp,
                alignment = TooltipAlignment.BOTTOM,
                textSize = 18.sp,
                tooltipText = "Tooltip",
                tooltipWidth = 0.dp,
                padding = 0.dp
            ),
            anchor = { tooltipState ->
                Button(onClick = { tooltipState.toggleVisibility() }) {
                    Text(text = "Button 1")
                }
            },
        )
    }
}

class TooltipState internal constructor(private var initialTooltipVisibility: Boolean) {
    private var isVisibleState by mutableStateOf(initialTooltipVisibility)
    val isVisible get() = isVisibleState
    fun toggleVisibility() {
        isVisibleState = isVisibleState.not()
    }
}

enum class TooltipAlignment {
    TOP,
    BOTTOM,
    START,
    END
}

@Composable
fun rememberTooltipState(initiallyTooltipVisible: Boolean): TooltipState {
    return remember {
        TooltipState(initiallyTooltipVisible)
    }
}