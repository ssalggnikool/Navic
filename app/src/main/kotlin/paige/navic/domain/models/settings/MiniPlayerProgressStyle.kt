package paige.navic.domain.models.settings

import paige.navic.R

enum class MiniPlayerProgressStyle(val displayName: Int) {
	Hidden(R.string.option_mini_player_progress_style_hidden),
	Visible(R.string.option_mini_player_progress_style_visible),
	Seekable(R.string.option_mini_player_progress_style_seekable)
}
