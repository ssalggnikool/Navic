package paige.navic.ui.screens.imageView.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import paige.navic.R
import paige.navic.di.LocalSnackBarState
import paige.navic.domain.manager.ShareManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.MoreVert
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.NestedTopBarButtonDefaults
import paige.navic.ui.components.layouts.NestedTopBarDefaults
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.util.Logger

@Composable
fun ImageViewScreenTopBar(
	bitmap: ImageBitmap? = null,
	title: String,
	onSetLoading: (Boolean) -> Unit
) {
	val resources = LocalResources.current
	val snackBarState = LocalSnackBarState.current
	val shareManager = koinInject<ShareManager>()

	val scope = rememberCoroutineScope()

	val containerColor = Color.Black
	val contentColor = Color.White
	val buttonColors = NestedTopBarButtonDefaults.colors(
		containerColor = containerColor,
		contentColor = contentColor
	)

	NestedTopBar(
		title = {},
		colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
		navigationAction = { NestedTopBarDefaults.NavigationAction(colors = buttonColors) },
		actions = {
			Box {
				var expanded by rememberSaveable { mutableStateOf(false) }
				TopBarButton(
					onClick = { expanded = true },
					colors = buttonColors
				) {
					Icon(
						imageVector = Icons.Outlined.MoreVert,
						contentDescription = stringResource(R.string.action_more)
					)
				}
				DropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false }
				) {
					DropdownMenuItem(
						text = { Text(stringResource(R.string.action_share)) },
						enabled = bitmap != null,
						onClick = {
							expanded = false
							scope.launch {
								onSetLoading(true)
								shareManager.shareImage(bitmap!!, "$title.png")
								onSetLoading(false)
							}
						}
					)
					DropdownMenuItem(
						text = { Text(stringResource(R.string.action_save)) },
						enabled = bitmap != null,
						onClick = {
							expanded = false
							scope.launch {
								onSetLoading(true)
								try {
									shareManager.saveImage(bitmap!!, "$title.png")
									onSetLoading(false)
									snackBarState.showSnackbar(
										message = resources.getString(R.string.notice_image_saved)
									)
								} catch (ex: Exception) {
									onSetLoading(false)
									Logger.e("ImageViewScreenTopBar", "couldn't save", ex)
									ex.message?.let {
										snackBarState.showSnackbar(message = it)
									}
								}
							}
						}
					)
				}
			}
		}
	)
}
