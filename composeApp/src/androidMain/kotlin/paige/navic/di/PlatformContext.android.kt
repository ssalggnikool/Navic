package paige.navic.di

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.ThemeMode

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun rememberPlatformContext(): PlatformContext {
	val view = LocalView.current
	val context = LocalContext.current
	val activity = LocalActivity.current!!
	val inDarkTheme = isSystemInDarkTheme()
	val preferenceManager = koinInject<PreferenceManager>()
	val isDark = remember(preferenceManager.themeMode) {
		when (preferenceManager.themeMode) {
			ThemeMode.System -> inDarkTheme
			ThemeMode.Dark -> true
			ThemeMode.Light -> false
		}
	}
	val sizeClass = calculateWindowSizeClass(activity)
	SideEffect {
		activity.window?.let { window ->
			WindowCompat.getInsetsController(window, view)
				.isAppearanceLightStatusBars = !isDark
		}
	}
	return remember(isDark, sizeClass) {
		object : PlatformContext {
			override val platformType = PlatformType.Android
			override val name = "Android ${Build.VERSION.SDK_INT}"
			override val appVersion: String =
				context.packageManager
					.getPackageInfo(context.packageName, 0)
					.versionName.toString()
			override val colorScheme
				get() = if (Build.VERSION.SDK_INT >= 31)
					if (isDark)
						if (preferenceManager.amoled)
							dynamicDarkColorScheme(context).copy(
								surface = Color.Black,
								onSurface = Color.White,
								background = Color.Black,
								onBackground = Color.White
							)
						else dynamicDarkColorScheme(context)
					else dynamicLightColorScheme(context)
				else
					if (isDark)
						darkColorScheme()
					else expressiveLightColorScheme()
			override val sizeClass = sizeClass
		}
	}
}
