/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import com.kyant.capsule.ContinuousRoundedRectangle
import org.koin.compose.koinInject
import paige.navic.domain.manager.runtime.PreferenceManager
import paige.navic.domain.model.settings.AnimationStyle
import paige.navic.domain.model.settings.ThemeMode

@Composable
fun NavicTheme(
	colorScheme: ColorScheme? = null,
	content: @Composable () -> Unit
) {
	val preferenceManager = koinInject<PreferenceManager>()
	val chosenTheme = preferenceManager.theme
	val chosenScheme = chosenTheme.colorScheme()
	val motionScheme = remember(preferenceManager.animationStyle) {
		when (preferenceManager.animationStyle) {
			AnimationStyle.Expressive -> MotionScheme.expressive()
			AnimationStyle.Standard -> MotionScheme.standard()
		}
	}
	val inDarkTheme = isSystemInDarkTheme()
	val isDark = remember(preferenceManager.themeMode) {
		when (preferenceManager.themeMode) {
			ThemeMode.System -> inDarkTheme
			ThemeMode.Dark -> true
			ThemeMode.Light -> false
		}
	}
	val activity = LocalActivity.current as? ComponentActivity

	activity?.enableEdgeToEdge(
		statusBarStyle = SystemBarStyle.auto(
			darkScrim = (colorScheme ?: chosenScheme).scrim.toArgb(),
			lightScrim = (colorScheme ?: chosenScheme).scrim.toArgb(),
			detectDarkMode = { isDark }
		)
	)

	MaterialExpressiveTheme(
		colorScheme = colorScheme
			?: chosenScheme,
		motionScheme = motionScheme,
		typography = typography(),
		shapes = Shapes(
			extraSmall = ContinuousRoundedRectangle(ShapeDefaults.ExtraSmall.topStart),
			small = ContinuousRoundedRectangle(ShapeDefaults.Small.topStart),
			medium = ContinuousRoundedRectangle(ShapeDefaults.Medium.topStart),
			large = ContinuousRoundedRectangle(ShapeDefaults.Large.topStart),
			extraLarge = ContinuousRoundedRectangle(ShapeDefaults.ExtraLarge.topStart),
			largeIncreased = ContinuousRoundedRectangle(ShapeDefaults.LargeIncreased.topStart),
			extraLargeIncreased = ContinuousRoundedRectangle(ShapeDefaults.ExtraLargeIncreased.topStart),
			extraExtraLarge = ContinuousRoundedRectangle(ShapeDefaults.ExtraExtraLarge.topStart)
		),
		content = content
	)
}
