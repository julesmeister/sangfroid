package com.julesmeister.sangfroid.core.designsystem.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.julesmeister.sangfroid.R

val SatoshiFontFamily = FontFamily(
    Font(
        resId = R.font.satoshi_regular,
        weight = FontWeight.Normal
    ),
    Font(
        resId = R.font.satoshi_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.satoshi_bold,
        weight = FontWeight.SemiBold
    ),
    Font(
        resId = R.font.satoshi_bold,
        weight = FontWeight.Bold
    ),
    Font(
        resId = R.font.satoshi_black,
        weight = FontWeight.Black
    )
)

private fun TextUnit.scaled(multiplier: Float): TextUnit {
    return (value * multiplier).sp
}

fun sangfroidTypography(
    fontScale: FontScale = FontScale.DEFAULT
): Typography {

    val scale = fontScale.multiplier

    return Typography(
        displayLarge = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp.scaled(scale),
            lineHeight = 52.sp.scaled(scale),
            letterSpacing = (-1.2).sp
        ),

        displayMedium = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp.scaled(scale),
            lineHeight = 44.sp.scaled(scale),
            letterSpacing = (-1.0).sp
        ),

        headlineLarge = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp.scaled(scale),
            lineHeight = 38.sp.scaled(scale),
            letterSpacing = (-0.6).sp
        ),

        headlineMedium = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp.scaled(scale),
            lineHeight = 32.sp.scaled(scale),
            letterSpacing = (-0.4).sp
        ),

        headlineSmall = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp.scaled(scale),
            lineHeight = 28.sp.scaled(scale)
        ),

        titleLarge = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp.scaled(scale),
            lineHeight = 26.sp.scaled(scale)
        ),

        titleMedium = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp.scaled(scale),
            lineHeight = 23.sp.scaled(scale)
        ),

        titleSmall = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp.scaled(scale),
            lineHeight = 20.sp.scaled(scale)
        ),

        bodyLarge = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp.scaled(scale),
            lineHeight = 25.sp.scaled(scale)
        ),

        bodyMedium = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp.scaled(scale),
            lineHeight = 22.sp.scaled(scale)
        ),

        bodySmall = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp.scaled(scale),
            lineHeight = 19.sp.scaled(scale)
        ),

        labelLarge = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp.scaled(scale),
            lineHeight = 19.sp.scaled(scale)
        ),

        labelMedium = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp.scaled(scale),
            lineHeight = 17.sp.scaled(scale)
        ),

        labelSmall = TextStyle(
            fontFamily = SatoshiFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp.scaled(scale),
            lineHeight = 15.sp.scaled(scale)
        )
    )
}