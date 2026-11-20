package paige.navic.preferences

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import org.koin.compose.koinInject
import paige.navic.domain.model.settings.Theme
import paige.navic.domain.model.settings.Theme.AppleMusic
import paige.navic.domain.model.settings.Theme.Dynamic
import paige.navic.domain.model.settings.Theme.Seeded
import paige.navic.domain.model.settings.Theme.Spotify
import paige.navic.domain.model.settings.Theme.iOS
import paige.navic.domain.model.settings.ThemeMode
import paige.navic.util.PlatformSupport

@Composable
fun Theme.colorScheme(): ColorScheme {
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
	val seededColorScheme = rememberDynamicColorScheme(
		seedColor = Color.hsv(
			hue = preferenceManager.paletteAccentH,
			saturation = preferenceManager.paletteAccentS,
			value = preferenceManager.paletteAccentV
		),
		isDark = isDark,
		specVersion = ColorSpec.SpecVersion.SPEC_2025,
		style = preferenceManager.paletteStyle,
		isAmoled = preferenceManager.amoled
	)

	return when (this) {
		Dynamic ->
			when {
				PlatformSupport.SUPPORTS_DYNAMIC_THEME -> if (isDark) {
					if (preferenceManager.amoled)
						dynamicDarkColorScheme(context).copy(
							surface = Color.Black,
							onSurface = Color.White,
							background = Color.Black,
							onBackground = Color.White
						)
					else {
						dynamicDarkColorScheme(context)
					}
				} else {
					dynamicLightColorScheme(context)
				}

				else -> seededColorScheme
			}

		Seeded -> seededColorScheme

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

private val IosRed = Color(255, 66, 69)

@Composable
fun lightIosColorScheme(
	accent: Color
): ColorScheme {
	return rememberDynamicColorScheme(
		primary = Color.White,
		isDark = false,
		isAmoled = true,
		specVersion = ColorSpec.SpecVersion.SPEC_2021,
		style = PaletteStyle.Content,
		modifyColorScheme = { scheme ->
			scheme.copy(
				primary = accent,
				onPrimary = Color.White,
				primaryContainer = accent.copy(alpha = .3f),
				onPrimaryContainer = accent,
				secondaryContainer = accent.copy(alpha = .3f),
				onSecondaryContainer = accent,
				secondary = accent,
				tertiaryContainer = accent.copy(alpha = .3f),
				onTertiaryContainer = accent,
				tertiary = accent,
				error = IosRed,
				onError = Color.White,
				errorContainer = IosRed,
				onErrorContainer = Color.White,
				surfaceVariant = Color(224, 221, 220)
			)
		}
	)
}

@Composable
fun darkIosColorScheme(
	accent: Color
): ColorScheme {
	return rememberDynamicColorScheme(
		primary = Color.White,
		isDark = true,
		isAmoled = true,
		specVersion = ColorSpec.SpecVersion.SPEC_2021,
		style = PaletteStyle.Content,
		modifyColorScheme = { scheme ->
			scheme.copy(
				primary = accent,
				onPrimary = Color.White,
				primaryContainer = accent.copy(alpha = .3f),
				onPrimaryContainer = accent,
				secondaryContainer = accent.copy(alpha = .3f),
				onSecondaryContainer = accent,
				secondary = accent,
				tertiaryContainer = accent.copy(alpha = .3f),
				onTertiaryContainer = accent,
				tertiary = accent,
				error = IosRed,
				onError = Color.White,
				errorContainer = IosRed,
				onErrorContainer = Color.White,
				surfaceVariant = Color(44, 44, 46),
				onSurfaceVariant = Color(142, 142, 147)
			)
		}
	)
}
