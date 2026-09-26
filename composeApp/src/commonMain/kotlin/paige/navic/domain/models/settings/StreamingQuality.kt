package paige.navic.domain.models.settings

import androidx.compose.runtime.Composable
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_quality_high
import navic.composeapp.generated.resources.option_quality_lossless
import navic.composeapp.generated.resources.option_quality_low
import navic.composeapp.generated.resources.option_quality_medium
import org.jetbrains.compose.resources.StringResource

enum class StreamingQuality(
	val displayName: StringResource,
	val bitrate: Int,
	val container: String?
) {
	Low(
		displayName = Res.string.option_quality_low,
		bitrate = 80,
		container = "opus"
	),
	Medium(
		displayName = Res.string.option_quality_medium,
		bitrate = 128,
		container = "opus"
	),
	High(
		displayName = Res.string.option_quality_high,
		bitrate = 192,
		container = "opus"
	),
	Lossless(
		displayName = Res.string.option_quality_lossless,
		bitrate = 0,
		container = null
	)
}

@Composable
fun StreamingQuality.description(): String? {
	return if (container != null) {
		"${bitrate}kbps, ${container.uppercase()}"
	} else {
		null
	}
}
