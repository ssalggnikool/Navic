package paige.navic.ui.screens.chat.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import dev.zt64.subsonic.api.model.ChatMessage
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import coil3.compose.LocalPlatformContext as LocalCoilPlatformContext

@Composable
fun ChatMessageItem(
	message: ChatMessage
) {
	val coilPlatformContext = LocalCoilPlatformContext.current

	val imageLoader = koinInject<ImageLoader>()
	val sessionManager = koinInject<SessionManager>()
	val preferenceManager = koinInject<PreferenceManager>()

	val avatarModel = remember(message.username, preferenceManager.customHeaders) {
		val networkHeaders = NetworkHeaders.Builder().apply {
			preferenceManager.customHeadersMap().forEach { (key, value) -> add(key, value) }
		}.build()
		ImageRequest.Builder(coilPlatformContext)
			.data(sessionManager.api.getAvatarUrl(username = message.username, auth = true))
			.httpHeaders(networkHeaders)
			.build()
	}

	ListItem(
		content = { Text(message.username) },
		supportingContent = { Text(message.message) },
		trailingContent = { Text(message.time.toString()) },
		leadingContent = {
			AsyncImage(
				model = avatarModel,
				imageLoader = imageLoader,
				contentDescription = null,
				modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.medium)
			)
		}
	)
}

