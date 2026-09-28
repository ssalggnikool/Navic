package paige.navic.domain.models.settings

import paige.navic.R

enum class QueueInfoType(val displayName: Int) {
	Full(R.string.option_queue_info_type_full),
	Remaining(R.string.option_queue_info_type_remining);
}
