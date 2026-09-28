package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import paige.navic.R

@Immutable
enum class DomainArtistListType(val displayName: Int) {
	AlphabeticalByName(R.string.option_sort_alphabetical_by_name),
	Random(R.string.option_sort_random)
}
