package paige.navic.ui.screens.imageView

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_share
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.di.LocalNavStack
import paige.navic.di.LocalSharedTransitionScope
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.ShareManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.MoreVert
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.NestedTopBarButtonDefaults
import paige.navic.ui.components.layouts.NestedTopBarDefaults
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.util.EmphasizedDecelerateEasing
import paige.navic.ui.util.toImageBitmap
import kotlin.math.abs
import kotlin.math.roundToInt
import coil3.compose.LocalPlatformContext as LocalCoilPlatformContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageViewScreen(
	coverArtId: String,
	title: String,
	sharedTransitionKey: String
) {
	val backStack = LocalNavStack.current
	val shareManager = koinInject<ShareManager>()

	val containerColor = Color.Black
	val contentColor = Color.White
	val buttonColors = NestedTopBarButtonDefaults.colors(
		containerColor = containerColor,
		contentColor = contentColor
	)

	val swipeThreshold = 100f
	val scope = rememberCoroutineScope()
	val offsetY = remember { Animatable(0f) }
	var dismissed by rememberSaveable { mutableStateOf(false) }

	val sessionManager = koinInject<SessionManager>()
	val coverArtUri = remember(coverArtId) {
		sessionManager.getCoverArtUrl(coverArtId)
	}

	val coilPlatformContext = LocalCoilPlatformContext.current
	val loader = SingletonImageLoader.get(coilPlatformContext)
	val model = remember(coverArtUri) {
		ImageRequest.Builder(coilPlatformContext)
			.data(coverArtUri)
			.memoryCacheKey(coverArtId)
			.diskCacheKey(coverArtId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.build()
	}
	var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
	var loadingShare by rememberSaveable { mutableStateOf(false) }

	LaunchedEffect(model) {
		val result = loader.execute(model)
		result.image?.toImageBitmap()?.let { imageBitmap ->
			bitmap = imageBitmap
		}
	}

	fun handleSwipeRelease() {
		if (dismissed) return
		scope.launch {
			if (abs(offsetY.value) > swipeThreshold) {
				dismissed = true
				backStack.removeLastOrNull()
				offsetY.animateTo(offsetY.value * 2)
			} else {
				offsetY.animateTo(0f)
			}
		}
	}

	Scaffold(
		topBar = {
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
										loadingShare = true
										shareManager.shareImage(bitmap!!, "$title.png")
										loadingShare = false
									}
								}
							)
						}
					}
				}
			)
		}
	) { _ ->
		Surface(
			modifier = Modifier.fillMaxSize(),
			color = containerColor,
			contentColor = contentColor
		) {
			Box(
				modifier = Modifier.fillMaxSize()
					.draggable(
						orientation = Orientation.Vertical,
						state = rememberDraggableState { delta ->
							scope.launch {
								offsetY.snapTo(offsetY.value + delta)
							}
						},
						onDragStopped = { handleSwipeRelease() }
					),
				contentAlignment = Alignment.Center
			) {
				with(LocalSharedTransitionScope.current) {
					CoverArt(
						coverArtId = coverArtId,
						contentDescription = null,
						modifier = Modifier
							.fillMaxSize()
							.requiredSizeIn(maxWidth = 420.dp, maxHeight = 420.dp)
							.aspectRatio(1f)
							.offset { IntOffset(x = 0, y = offsetY.value.roundToInt()) }
							.sharedElement(
								sharedContentState = this@with.rememberSharedContentState(
									sharedTransitionKey
								),
								boundsTransform = BoundsTransform { _, _ ->
									tween(
										durationMillis = 500,
										easing = EmphasizedDecelerateEasing
									)
								},
								animatedVisibilityScope = LocalNavAnimatedContentScope.current
							),
						crossfadeMs = 0,
						shape = RectangleShape
					)
				}
				if (loadingShare) {
					Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.5f)))
					ContainedLoadingIndicator(Modifier.size(48.dp))
					NavigationBackHandler(
						state = rememberNavigationEventState(NavigationEventInfo.None),
						onBackCompleted = {}
					)
				}
			}
		}
	}
}
