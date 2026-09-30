/*
 * Navic, a Subsonic music streaming app for Android
 * Copyright (c) 2026 paige
 * SPDX-License-Identifier: GPL-3.0-only
 */

package paige.navic.domain.model.settings

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import dev.zt64.compose.pipette.HsvColor
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.domain.manager.PreferenceManager
import paige.navic.ui.util.darkIosColorScheme
import paige.navic.ui.util.lightIosColorScheme

/**
 * Theme choices that the user can choose from
 */
enum class Theme(val title: Int) {

	/**
	 * The app will be themed based on a "seed" colour.
	 *
	 * When this is selected, `accentColor(H/S/V)` settings
	 * will be exposed in the UI as a colour picker.
	 */
	Seeded(R.string.theme_seeded),

	/**
	 * The app will be themed based on whatever the user
	 * chose in system settings. Android only.
	 */
	Dynamic(R.string.theme_dynamic),

	/**
	 * The app will be themed according to Apple's HIG.
	 */
	@Suppress("EnumEntryName")
	iOS(R.string.theme_ios),

	/**
	 * The same as iOS, but with a pink-ish accent.
	 */
	AppleMusic(R.string.theme_apple_music),

	/**
	 * The same as iOS, but with a green accent.
	 */
	Spotify(R.string.theme_spotify);

	@Composable
	fun colorScheme(): ColorScheme {
		val context = LocalContext.current
		val inDarkTheme = isSystemInDarkTheme()
		val preferenceManager = koinInject<PreferenceManager>()
		val isDark = remember(preferenceManager.themeMode) {
			when (preferenceManager.themeMode) {
				ThemeMode.System -> inDarkTheme
				ThemeMode.Dark -> true
				ThemeMode.Light -> false
			}
		}
		return when (this) {
			Dynamic ->
				when {
					Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (isDark) {
						if (preferenceManager.amoled)
							dynamicDarkColorScheme(context).copy(
								surface = Color.Black,
								onSurface = Color.White,
								background = Color.Black,
								onBackground = Color.White
							)
						else dynamicDarkColorScheme(context)
					} else dynamicLightColorScheme(context)

					else -> if (isDark) darkColorScheme() else lightColorScheme()
				}

			Seeded -> rememberDynamicColorScheme(
				seedColor = HsvColor(
					hue = preferenceManager.paletteAccentH,
					saturation = 1f,
					value = 1f
				).toColor(),
				isDark = isDark,
				specVersion = ColorSpec.SpecVersion.SPEC_2025,
				style = preferenceManager.paletteStyle,
				isAmoled = preferenceManager.amoled
			)

			iOS -> if (isDark)
				darkIosColorScheme(Color(0, 145, 255))
			else lightIosColorScheme(Color(0, 136, 255))

			AppleMusic -> if (isDark)
				darkIosColorScheme(Color(255, 55, 95))
			else lightIosColorScheme(Color(255, 45, 85))

			Spotify -> if (isDark)
				darkIosColorScheme(Color(30, 215, 96))
			else lightIosColorScheme(Color(30, 215, 96))
		}
	}

	fun isMaterialLike(): Boolean = when (this) {
		Dynamic,
		Seeded -> true

		else -> false
	}
}
