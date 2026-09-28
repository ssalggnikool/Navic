package paige.navic.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import paige.navic.R
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainSongListType
import paige.navic.domain.models.lyrics.LyricsProvider
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Composable
fun Duration.label(): String {
	val hours = inWholeHours.toInt()
	val minutes = (this - hours.hours).inWholeMinutes.toInt()

	return when {
		hours > 0 && minutes > 0 ->
			"${pluralStringResource(R.plurals.count_hours, hours, hours)} ${
				pluralStringResource(
					R.plurals.count_minutes,
					minutes,
					minutes
				)
			}"

		hours > 0 ->
			pluralStringResource(R.plurals.count_hours, hours, hours)

		else ->
			pluralStringResource(R.plurals.count_minutes, max(1, minutes), max(1, minutes))
	}
}

@Composable
fun DomainAlbumListType.label() = when (this) {
	DomainAlbumListType.Random -> stringResource(R.string.option_sort_random)
	DomainAlbumListType.Newest -> stringResource(R.string.option_sort_newest)
	DomainAlbumListType.Frequent -> stringResource(R.string.option_sort_frequent)
	DomainAlbumListType.Recent -> stringResource(R.string.option_sort_recent)
	DomainAlbumListType.AlphabeticalByName -> stringResource(R.string.option_sort_alphabetical_by_name)
	DomainAlbumListType.AlphabeticalByArtist -> stringResource(R.string.option_sort_alphabetical_by_artist)
	DomainAlbumListType.Highest -> stringResource(R.string.option_sort_rating)
	DomainAlbumListType.Year -> stringResource(R.string.option_sort_year)
	is DomainAlbumListType.ByGenre -> stringResource(R.string.option_sort_by_genre)
	is DomainAlbumListType.ByYear -> stringResource(R.string.option_sort_by_year)
}

@Composable
fun DomainSongListType.label() = when (this) {
	is DomainSongListType.ByArtist -> stringResource(R.string.option_sort_by_genre)
	is DomainSongListType.ByGenre -> stringResource(R.string.option_sort_by_genre)
	DomainSongListType.FrequentlyPlayed -> stringResource(R.string.option_sort_frequent)
	DomainSongListType.Newest -> stringResource(R.string.option_sort_newest)
	DomainSongListType.Random -> stringResource(R.string.option_sort_random)
	DomainSongListType.Rating -> stringResource(R.string.option_sort_rating)
	DomainSongListType.Year -> stringResource(R.string.option_sort_by_year)
}

fun PaletteStyle.label(): String = when (this) {
	PaletteStyle.TonalSpot -> "Tonal Spot"
	PaletteStyle.Neutral -> "Neutral"
	PaletteStyle.Vibrant -> "Vibrant"
	PaletteStyle.Expressive -> "Expressive"
	PaletteStyle.Rainbow -> "Rainbow"
	PaletteStyle.FruitSalad -> "Fruit Salad"
	PaletteStyle.Monochrome -> "Monochrome"
	PaletteStyle.Fidelity -> "Fidelity"
	PaletteStyle.Content -> "Content"
}

fun ColorSpec.SpecVersion.label() = when (this) {
	ColorSpec.SpecVersion.SPEC_2021 -> "Material 3 (2021)"
	ColorSpec.SpecVersion.SPEC_2025 -> "Expressive (2025)"
}

fun LyricsProvider.Id.label() = when (this) {
	LyricsProvider.Id.SUBSONIC -> "Subsonic"
	LyricsProvider.Id.LYRICS_PLUS -> "LyricsPlus"
	LyricsProvider.Id.LRCLIB -> "LRCLIB"
}
