package paige.navic.domain.models.snackbars


data class PlayerEvent(
	val resource: Int,
	val args: List<Any> = emptyList()
)
