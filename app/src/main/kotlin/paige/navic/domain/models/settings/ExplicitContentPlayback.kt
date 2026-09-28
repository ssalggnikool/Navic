package paige.navic.domain.models.settings

import paige.navic.R

enum class ExplicitContentPlayback(val displayName: Int) {
	Allowed(R.string.option_explicit_playback_allowed),
	Skip(R.string.option_explicit_playback_skip),
	SkipForThisSession(R.string.option_explicit_playback_skip_session)
}
