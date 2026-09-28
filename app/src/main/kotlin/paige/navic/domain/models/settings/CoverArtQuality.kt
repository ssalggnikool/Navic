package paige.navic.domain.models.settings

import paige.navic.R

enum class CoverArtQuality(
	val displayName: Int,
	val value: Int
) {
	Low(R.string.option_quality_low, 512),
	Medium(R.string.option_quality_medium, 1024),
	High(R.string.option_quality_high, 4096)
}
