package paige.navic.domain.models.settings

import paige.navic.R

enum class NowPlayingBackgroundStyle(val displayName: Int) {
	Static(R.string.option_now_playing_background_style_static),
	Dynamic(R.string.option_now_playing_background_style_dynamic)
}
