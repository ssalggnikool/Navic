package paige.navic.domain.models.settings

import paige.navic.R

enum class OfflineMode(val displayName: Int) {
	Auto(R.string.option_offline_mode_auto),
	Forced(R.string.option_offline_mode_forced),
	NoWiFi(R.string.option_offline_mode_no_wifi),
}
