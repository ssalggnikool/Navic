package paige.navic.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_genres
import navic.composeapp.generated.resources.title_library
import navic.composeapp.generated.resources.title_playlists
import navic.composeapp.generated.resources.title_radios
import navic.composeapp.generated.resources.title_search
import navic.composeapp.generated.resources.title_songs
import navic.composeapp.generated.resources.title_statistics
import org.jetbrains.compose.resources.StringResource
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
	val label: StringResource
) {
	LIBRARY(
		destination = Screen.Library(),
		iconFilled = Icons.Filled.LibraryMusic,
		iconOutlined = Icons.Outlined.LibraryMusic,
		label = Res.string.title_library
	),
	ALBUMS(
		destination = Screen.AlbumList(),
		iconFilled = Icons.Filled.Album,
		iconOutlined = Icons.Outlined.Album,
		label = Res.string.title_albums
	),
	PLAYLISTS(
		destination = Screen.PlaylistList(),
		iconFilled = Icons.Outlined.PlaylistPlay,
		label = Res.string.title_playlists
	),
	ARTISTS(
		destination = Screen.ArtistList(),
		iconFilled = Icons.Filled.Artist,
		iconOutlined = Icons.Outlined.Artist,
		label = Res.string.title_artists
	),
	SEARCH(
		destination = Screen.Search(),
		iconFilled = Icons.Outlined.Search,
		iconOutlined = Icons.Outlined.Search,
		label = Res.string.title_search
	),
	GENRES(
		destination = Screen.GenreList(),
		iconFilled = Icons.Filled.Genre,
		iconOutlined = Icons.Outlined.Genre,
		label = Res.string.title_genres
	),
	SONGS(
		destination = Screen.SongList(),
		iconFilled = Icons.Outlined.Note,
		iconOutlined = Icons.Outlined.Note,
		label = Res.string.title_songs
	),
	RADIOS(
		destination = Screen.RadioList(),
		iconFilled = Icons.Filled.Radio,
		iconOutlined = Icons.Outlined.Radio,
		label = Res.string.title_radios
	),
	STATISTICS(
		destination = Screen.Statistics(),
		iconFilled = Icons.Outlined.Statistics,
		label = Res.string.title_statistics
	)
}
