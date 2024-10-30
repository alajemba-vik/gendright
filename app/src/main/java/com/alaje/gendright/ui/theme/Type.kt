package com.alaje.gendright.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.alaje.gendright.R


private val piazzollaFamily = FontFamily(
    Font(R.font.piazzolla_bold, weight = FontWeight.Bold),
)

private val redHatTextFamily = FontFamily(
    Font(R.font.red_hat_text, FontWeight.Normal),
    Font(R.font.red_hat_text_bold, FontWeight.Bold)
)

private val interFamily = FontFamily(
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)

val Typography = Typography(
    bodySmall = TextStyle(
        fontFamily = redHatTextFamily,
        fontSize = 14.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 54.sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineSmall = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
    ),
    labelSmall = TextStyle(
        fontFamily = redHatTextFamily,
        fontSize = 12.sp,
        color = Grey600,
    ),
    titleLarge = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 24.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = interFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)
