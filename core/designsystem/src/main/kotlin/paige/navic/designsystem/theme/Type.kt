/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import paige.navic.core.designsystem.R
import paige.navic.domain.model.settings.FontOption
import paige.navic.preferences.PreferenceManager
import paige.navic.util.PlatformSupport

private val defaultTypography = Typography()

// https://github.com/tribalfs/oneui-design/blob/1befedd193025f2271f741e10fd12bc0f101b17e/lib/src/main/java/dev/oneuiproject/oneui/utils/TypefaceUtils.kt
@Composable
public fun oneUiSans(): FontFamily {
	return remember {
		FontFamily(
			Font(DeviceFontFamilyName("sec"), weight = FontWeight.Light),
			Font(DeviceFontFamilyName("sec"), weight = FontWeight.Normal),
			Font(DeviceFontFamilyName("sec"), weight = FontWeight.Medium),
			Font(DeviceFontFamilyName("sec"), weight = FontWeight.SemiBold),
			Font(DeviceFontFamilyName("sec"), weight = FontWeight.Bold)
		)
	}
}

@Composable
public fun googleSans(
	grade: Int = 0,
	width: Float = 100f,
	round: Float = 0f
): FontFamily {
	val font = Font(
		R.font.google_sans,
		variationSettings = FontVariation.Settings(
			FontVariation.grade(grade),
			FontVariation.width(width),
			FontVariation.Setting("ROND", round)
		)
	)
	return remember { FontFamily(font) }
}

@Composable
public fun defaultFont(
	grade: Int = 0,
	width: Float = 100f,
	round: Float = 0f
): FontFamily {
	val preferenceManager = koinInject<PreferenceManager>()
	val googleSans = googleSans(grade, width, round)
	val oneUiSans = oneUiSans()
	return remember(preferenceManager.font, preferenceManager.fontPath) {
		when (preferenceManager.font) {
			FontOption.System -> FontFamily.Default
			FontOption.GoogleSans -> googleSans
			FontOption.OneUISans -> if (PlatformSupport.SUPPORTS_ONE_UI_SANS) oneUiSans else FontFamily.Default
			FontOption.Custom -> FontFamily.Default
		}
	}
}

private fun TextStyle.withFontFamily(fontFamily: FontFamily, useOneUiSans: Boolean) = copy(
	fontFamily = fontFamily,
	letterSpacing = if (useOneUiSans && letterSpacing > 0.sp) 0.2.sp else letterSpacing
)

@Composable
public fun typography(): Typography {
	val preferenceManager = koinInject<PreferenceManager>()
	val fontFamily = defaultFont()

	val useOneUiSans = preferenceManager.font == FontOption.OneUISans && PlatformSupport.SUPPORTS_ONE_UI_SANS
	return Typography(
		displayLarge = defaultTypography.displayLarge.withFontFamily(fontFamily, useOneUiSans),
		displayLargeEmphasized = defaultTypography.displayLargeEmphasized.withFontFamily(fontFamily, useOneUiSans),
		displayMedium = defaultTypography.displayMedium.withFontFamily(fontFamily, useOneUiSans),
		displayMediumEmphasized = defaultTypography.displayMediumEmphasized.withFontFamily(fontFamily, useOneUiSans),
		displaySmall = defaultTypography.displaySmall.withFontFamily(fontFamily, useOneUiSans),
		displaySmallEmphasized = defaultTypography.displaySmallEmphasized.withFontFamily(fontFamily, useOneUiSans),

		headlineLarge = defaultTypography.headlineLarge.withFontFamily(fontFamily, useOneUiSans),
		headlineLargeEmphasized = defaultTypography.headlineLargeEmphasized.withFontFamily(fontFamily, useOneUiSans),
		headlineMedium = defaultTypography.headlineMedium.withFontFamily(fontFamily, useOneUiSans),
		headlineMediumEmphasized = defaultTypography.headlineMediumEmphasized.withFontFamily(fontFamily, useOneUiSans),
		headlineSmall = defaultTypography.headlineSmall.withFontFamily(fontFamily, useOneUiSans),
		headlineSmallEmphasized = defaultTypography.headlineSmallEmphasized.withFontFamily(fontFamily, useOneUiSans),

		titleLarge = defaultTypography.titleLarge.withFontFamily(fontFamily, useOneUiSans),
		titleLargeEmphasized = defaultTypography.titleLargeEmphasized.withFontFamily(fontFamily, useOneUiSans),
		titleMedium = defaultTypography.titleMedium.withFontFamily(fontFamily, useOneUiSans),
		titleMediumEmphasized = defaultTypography.titleMediumEmphasized.withFontFamily(fontFamily, useOneUiSans),
		titleSmall = defaultTypography.titleSmall.withFontFamily(fontFamily, useOneUiSans),
		titleSmallEmphasized = defaultTypography.titleSmallEmphasized.withFontFamily(fontFamily, useOneUiSans),

		bodyLarge = defaultTypography.bodyLarge.withFontFamily(fontFamily, useOneUiSans),
		bodyLargeEmphasized = defaultTypography.bodyLargeEmphasized.withFontFamily(fontFamily, useOneUiSans),
		bodyMedium = defaultTypography.bodyMedium.withFontFamily(fontFamily, useOneUiSans),
		bodyMediumEmphasized = defaultTypography.bodyMediumEmphasized.withFontFamily(fontFamily, useOneUiSans),
		bodySmall = defaultTypography.bodySmall.withFontFamily(fontFamily, useOneUiSans),
		bodySmallEmphasized = defaultTypography.bodySmallEmphasized.withFontFamily(fontFamily, useOneUiSans),

		labelLarge = defaultTypography.labelLarge.withFontFamily(fontFamily, useOneUiSans),
		labelLargeEmphasized = defaultTypography.labelLargeEmphasized.withFontFamily(fontFamily, useOneUiSans),
		labelMedium = defaultTypography.labelMedium.withFontFamily(fontFamily, useOneUiSans),
		labelMediumEmphasized = defaultTypography.labelMediumEmphasized.withFontFamily(fontFamily, useOneUiSans),
		labelSmall = defaultTypography.labelSmall.withFontFamily(fontFamily, useOneUiSans),
		labelSmallEmphasized = defaultTypography.labelSmallEmphasized.withFontFamily(fontFamily, useOneUiSans)
	)
}
