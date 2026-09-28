package paige.navic.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import paige.navic.R
import paige.navic.icons.Icons
import paige.navic.icons.filled.Album
import paige.navic.icons.filled.Artist
import paige.navic.icons.filled.Genre
import paige.navic.icons.filled.LibraryMusic
import paige.navic.icons.filled.Radio
import paige.navic.icons.outlined.Album
import paige.navic.icons.outlined.Artist
import paige.navic.icons.outlined.Genre
import paige.navic.icons.outlined.LibraryMusic
import paige.navic.icons.outlined.Note
import paige.navic.icons.outlined.PlaylistPlay
import paige.navic.icons.outlined.Radio
import paige.navic.icons.outlined.Search
import paige.navic.icons.outlined.Statistics

enum class NavigationTab(
	val destination: Screen,
	val iconFilled: ImageVector,
	val iconOutlined: ImageVector = iconFilled,
	val label: Int
) {
	LIBRARY(
		destination = Screen.Library(),
		iconFilled = Icons.Filled.LibraryMusic,
		iconOutlined = Icons.Outlined.LibraryMusic,
		label = R.string.title_library
	),
	ALBUMS(
		destination = Screen.AlbumList(),
		iconFilled = Icons.Filled.Album,
		iconOutlined = Icons.Outlined.Album,
		label = R.string.title_albums
	),
	PLAYLISTS(
		destination = Screen.PlaylistList(),
		iconFilled = Icons.Outlined.PlaylistPlay,
		label = R.string.title_playlists
	),
	ARTISTS(
		destination = Screen.ArtistList(),
		iconFilled = Icons.Filled.Artist,
		iconOutlined = Icons.Outlined.Artist,
		label = R.string.title_artists
	),
	SEARCH(
		destination = Screen.Search(),
		iconFilled = Icons.Outlined.Search,
		iconOutlined = Icons.Outlined.Search,
		label = R.string.title_search
	),
	GENRES(
		destination = Screen.GenreList(),
		iconFilled = Icons.Filled.Genre,
		iconOutlined = Icons.Outlined.Genre,
		label = R.string.title_genres
	),
	SONGS(
		destination = Screen.SongList(),
		iconFilled = Icons.Outlined.Note,
		iconOutlined = Icons.Outlined.Note,
		label = R.string.title_songs
	),
	RADIOS(
		destination = Screen.RadioList(),
		iconFilled = Icons.Filled.Radio,
		iconOutlined = Icons.Outlined.Radio,
		label = R.string.title_radios
	),
	STATISTICS(
		destination = Screen.Statistics(),
		iconFilled = Icons.Outlined.Statistics,
		label = R.string.title_statistics
	)
}
