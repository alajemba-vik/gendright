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
    Font(R.font.red_hat_text_semibold, FontWeight.SemiBold),
    Font(R.font.red_hat_text_bold, FontWeight.Bold)
)

private val interFamily = FontFamily(
    Font(R.font.inter_semibold, FontWeight.SemiBold)
)

val Typography = Typography(
    // Body
    bodySmall = TextStyle(
        fontFamily = redHatTextFamily,
        fontSize = 14.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = redHatTextFamily,
        fontSize = 14.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = redHatTextFamily,
        fontSize = 16.sp,
    ),

    // Headlines
    headlineSmall = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineMedium = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineLarge = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 54.sp,
        fontWeight = FontWeight.Bold,
    ),

    // Labels
    labelSmall = TextStyle(
        fontFamily = redHatTextFamily,
        fontSize = 12.sp,
        color = Grey600,
    ),
    labelMedium = TextStyle(
        fontFamily = interFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    labelLarge = TextStyle(
        fontFamily = interFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    ),

    // Titles
    titleSmall = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 16.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 18.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 24.sp,
    ),

    // Displays
    displayLarge = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 96.sp,
        fontWeight = FontWeight.Bold,
    ),
    displayMedium = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 60.sp,
        fontWeight = FontWeight.Bold,
    ),
    displaySmall = TextStyle(
        fontFamily = piazzollaFamily,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
    )
)
/*
*
* val displayLarge: TextStyle = TypographyTokens. DisplayLarge,
    val displayMedium: TextStyle = TypographyTokens. DisplayMedium,
    val displaySmall: TextStyle = TypographyTokens. DisplaySmall,
    val headlineLarge: TextStyle = TypographyTokens. HeadlineLarge,
    val headlineMedium: TextStyle = TypographyTokens. HeadlineMedium,
    val headlineSmall: TextStyle = TypographyTokens. HeadlineSmall,
    val titleLarge: TextStyle = TypographyTokens. TitleLarge,
    val titleMedium: TextStyle = TypographyTokens. TitleMedium,
    val titleSmall: TextStyle = TypographyTokens. TitleSmall,
    val bodyLarge: TextStyle = TypographyTokens. BodyLarge,
    val bodyMedium: TextStyle = TypographyTokens. BodyMedium,
    val bodySmall: TextStyle = TypographyTokens. BodySmall,
    val labelLarge: TextStyle = TypographyTokens. LabelLarge,
    val labelMedium: TextStyle = TypographyTokens. LabelMedium,
    val labelSmall: TextStyle = TypographyTokens. LabelSmall
)
* */