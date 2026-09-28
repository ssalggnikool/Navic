package paige.navic.domain.models.settings

import paige.navic.R

enum class BottomBarCollapseMode(val displayName: Int) {
	Never(R.string.option_bottom_bar_collapse_mode_never),
	OnScroll(R.string.option_bottom_bar_collapse_mode_on_scroll)
}
