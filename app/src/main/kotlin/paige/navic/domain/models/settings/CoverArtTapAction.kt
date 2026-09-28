package paige.navic.domain.models.settings

import paige.navic.R

enum class CoverArtTapAction(val displayName: Int) {
	Disabled(R.string.option_cover_art_action_disabled),
	ShowLyrics(R.string.option_cover_art_action_show_lyrics)
}
