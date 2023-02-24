package br.com.frazo.janac.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


data class Dimensions(
    val smallCornerRadius: Dp = 4.dp,
    val mediumCornerRadius: Dp = 8.dp,
    val largeCornerRadius: Dp = 16.dp,
    val buttonBorderStrokeWidth: Dp = 2.dp,
    val mediumButtonHeight: Dp = 40.dp,
    val smallButtonHeight: Dp = 20.dp,
    val largeButtonHeight: Dp = 60.dp,
    val defaultButtonHeight: Dp = 0.dp,
    val cardElevation: Dp = 10.dp,
    val mediumIconSize: Dp = 48.dp,
    val largeIconSize: Dp = 96.dp,
    val floatingActionButtonElevation: Dp = 8.dp,
    val dialogContentMinHeight: Dp = 60.dp
)

val LocalDimensions = compositionLocalOf { Dimensions() }

val MaterialTheme.dimensions: Dimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalDimensions.current