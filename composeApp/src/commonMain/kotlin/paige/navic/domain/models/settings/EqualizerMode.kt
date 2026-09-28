package paige.navic.domain.models.settings

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_equalizer_mode_builtin
import navic.composeapp.generated.resources.option_equalizer_mode_disabled
import navic.composeapp.generated.resources.option_equalizer_mode_external
import org.jetbrains.compose.resources.StringResource

// Which equalizer processes Navic's audio session. Builtin/External need to be mutually exclusive,
// otherwise they will fight for effect control and cause audio issues
enum class EqualizerMode(val displayName: StringResource) {
	Disabled(Res.string.option_equalizer_mode_disabled),
	BuiltIn(Res.string.option_equalizer_mode_builtin),
	External(Res.string.option_equalizer_mode_external)
}
