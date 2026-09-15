package paige.navic.domain.models.settings

import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_queue_info_type_full
import navic.composeapp.generated.resources.option_queue_info_type_remining
import org.jetbrains.compose.resources.StringResource

enum class QueueInfoType(val displayName: StringResource) {
	Full(Res.string.option_queue_info_type_full),
	Remaining(Res.string.option_queue_info_type_remining);
}
