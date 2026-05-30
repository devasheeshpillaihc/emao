package com.spendsense.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SpendSenseShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Custom shapes used across the app
val CardShape = RoundedCornerShape(16.dp)
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
val ChipShape = RoundedCornerShape(20.dp)
val ButtonShape = RoundedCornerShape(12.dp)
val ProgressBarShape = RoundedCornerShape(8.dp)
