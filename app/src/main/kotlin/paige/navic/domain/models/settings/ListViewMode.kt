package paige.navic.domain.models.settings

import androidx.compose.ui.graphics.vector.ImageVector
import paige.navic.R
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Grid
import paige.navic.icons.outlined.List

enum class ListViewMode(val displayName: Int, val icon: ImageVector) {
	Grid(R.string.option_list_view_mode_grid, Icons.Outlined.Grid),
	List(R.string.option_list_view_mode_list, Icons.Outlined.List)
}
