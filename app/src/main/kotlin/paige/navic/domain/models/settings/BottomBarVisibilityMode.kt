package paige.navic.domain.models.settings

import androidx.annotation.StringRes
import paige.navic.R

enum class BottomBarVisibilityMode(@StringRes val displayName: Int) {
	Default(R.string.option_bottom_bar_visibility_mode_default),
	AllScreens(R.string.option_bottom_bar_visibility_mode_all_screens)
}
