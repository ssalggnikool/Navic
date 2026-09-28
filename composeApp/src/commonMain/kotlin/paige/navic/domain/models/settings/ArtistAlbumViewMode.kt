package paige.navic.domain.models.settings

import androidx.compose.ui.graphics.vector.ImageVector
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_artist_album_view_mode_carousel
import navic.composeapp.generated.resources.option_list_view_mode_grid
import navic.composeapp.generated.resources.option_list_view_mode_list
import org.jetbrains.compose.resources.StringResource
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Carousel
import paige.navic.icons.outlined.Grid
import paige.navic.icons.outlined.List

enum class ArtistAlbumViewMode(val displayName: StringResource, val icon: ImageVector) {
	Carousel(Res.string.option_artist_album_view_mode_carousel, Icons.Outlined.Carousel),
	List(Res.string.option_list_view_mode_list, Icons.Outlined.List),
	Grid(Res.string.option_list_view_mode_grid, Icons.Outlined.Grid)
}
