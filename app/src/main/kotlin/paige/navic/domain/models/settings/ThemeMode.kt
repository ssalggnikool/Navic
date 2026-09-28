package paige.navic.domain.models.settings

import paige.navic.R


enum class ThemeMode(val title: Int) {
	System(R.string.theme_mode_system),
	Dark(R.string.theme_mode_dark),
	Light(R.string.theme_mode_light)
}
