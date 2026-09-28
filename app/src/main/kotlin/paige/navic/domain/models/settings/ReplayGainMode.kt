package paige.navic.domain.models.settings

import paige.navic.R

enum class ReplayGainMode(val displayName: Int) {
	Off(R.string.option_off),
	Track(R.string.info_track_replay_gain),
	Album(R.string.info_album_replay_gain),
	Dynamic(R.string.info_dynamic_replay_gain)
}
