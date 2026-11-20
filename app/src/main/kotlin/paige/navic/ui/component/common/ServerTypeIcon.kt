package paige.navic.ui.component.common

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import paige.navic.domain.server.ServerType
import paige.navic.icons.Icons
import paige.navic.icons.brand.Jellyfin
import paige.navic.icons.brand.Navidrome
import paige.navic.icons.brand.Subsonic

@Composable
fun ServerTypeIcon(
	serverType: ServerType,
	modifier: Modifier = Modifier
) {
	Icon(
		imageVector = when (serverType) {
			ServerType.SUBSONIC -> Icons.Brand.Subsonic
			ServerType.NAVIDROME -> Icons.Brand.Navidrome
			ServerType.JELLYFIN -> Icons.Brand.Jellyfin
		},
		contentDescription = serverType.prettyName,
		modifier = modifier
	)
}
