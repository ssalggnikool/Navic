package paige.navic.domain.models.settings

import paige.navic.R

enum class NowPlayingSliderStyle(val displayName: Int) {
	Flat(R.string.option_now_playing_slider_style_flat),
	Squiggly(R.string.option_now_playing_slider_style_squiggly),
	Slim(R.string.option_now_playing_slider_style_slim),
	Yoyo(R.string.option_now_playing_slider_style_yoyo)
}
