package paige.navic.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

object PlatformSupport {
	// used by NavicTheme and ThemesScreen
	@ChecksSdkIntAtLeast(Build.VERSION_CODES.S)
	val SUPPORTS_DYNAMIC_THEME: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
	@ChecksSdkIntAtLeast(Build.VERSION_CODES.Q)
	val SUPPORTS_DARK_THEME: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

	// used by BackgroundBlend
	@ChecksSdkIntAtLeast(Build.VERSION_CODES.S)
	val SUPPORTS_COMPOSE_BLUR: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
	@ChecksSdkIntAtLeast(Build.VERSION_CODES.O)
	val IS_BITMAP_HARDWARE_RENDERING_BROKEN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.S
}
