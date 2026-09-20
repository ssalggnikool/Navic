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
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_save
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.notice_image_saved
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
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
						contentDescription = stringResource(Res.string.action_more)
					)
				}
				DropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false }
				) {
					DropdownMenuItem(
						text = { Text(stringResource(Res.string.action_share)) },
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
						text = { Text(stringResource(Res.string.action_save)) },
						enabled = bitmap != null,
						onClick = {
							expanded = false
							scope.launch {
								onSetLoading(true)
								try {
									shareManager.saveImage(bitmap!!, "$title.png")
									onSetLoading(false)
									snackBarState.showSnackbar(
										message = getString(Res.string.notice_image_saved)
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
