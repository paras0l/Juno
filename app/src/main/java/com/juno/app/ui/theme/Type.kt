package com.juno.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.juno.app.R

val ZcoolKuaiLe = FontFamily(
    Font(R.font.zcool_kuaile, FontWeight.Normal)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = ZcoolKuaiLe,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp, // 1.6x
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ZcoolKuaiLe,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp, // 1.6x
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ZcoolKuaiLe,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 20.sp, // ~1.6x
        letterSpacing = 0.4.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = ZcoolKuaiLe,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ZcoolKuaiLe,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ZcoolKuaiLe,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp, // Very large titles
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp // Tighter tracking for large titles
    ),
    titleMedium = TextStyle(
        fontFamily = ZcoolKuaiLe,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ZcoolKuaiLe,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
