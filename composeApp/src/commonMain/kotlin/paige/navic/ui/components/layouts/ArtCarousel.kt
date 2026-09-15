package paige.navic.ui.components.layouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import kotlinx.collections.immutable.ImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_see_all
import navic.composeapp.generated.resources.count_plays
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import paige.navic.di.LocalNavStack
import paige.navic.ui.components.common.CoverArt
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp
import paige.navic.util.toSummaryString
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ArtCarousel(
	title: String,
	items: ImmutableList<T>,
	destination: NavKey? = null,
	content: @Composable (item: T) -> Unit
) {
	val backStack = LocalNavStack.current

	if (items.isNotEmpty()) {
		Column {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Text(
					title,
					style = MaterialTheme.typography.titleMediumEmphasized,
					fontWeight = FontWeight(600),
					modifier = Modifier.heightIn(min = 32.dp).padding(top = 12.dp)
				)
				if (destination != null) {
					Text(
						stringResource(Res.string.action_see_all),
						fontSize = 12.sp,
						color = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.heightIn(min = 32.dp).padding(top = 12.dp)
							.clickable(onClick = dropUnlessResumed {
								backStack.add(destination)
							})
					)
				}
			}
			LazyRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 16.dp, bottom = 16.dp),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(horizontal = 16.dp)
			) {
				items(items) { item ->
					content(item)
				}
			}
		}
	}
}

@Composable
fun ArtCarouselItem(
	coverArtId: String?,
	title: String,
	subtitle: String? = null,
	playCount: Int? = null,
	duration: Duration? = null,
	contentDescription: String?,
	onSelect: () -> Unit = {},
	onClick: () -> Unit = {}
) {
	val focusManager = LocalFocusManager.current

	Column(
		modifier = Modifier
			.width(150.dp)
			.clip(MaterialTheme.shapes.large)
			.combinedClickable(
				onClick = {
					focusManager.clearFocus(true)
					onClick()
				},
				onLongClick = onSelect
			)
			.semantics {
				this.contentDescription = contentDescription ?: title
			}
	) {
		CoverArt(
			coverArtId = coverArtId,
			contentDescription = null,
			modifier = Modifier
				.fillMaxWidth()
				.clip(MaterialTheme.shapes.large)
		)

		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Medium,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.padding(top = 8.dp, start = 4.dp, end = 4.dp)
		)

		subtitle?.let {
			Text(
				text = subtitle,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier
					.fillMaxWidth()
					.padding(start = 4.dp, end = 4.dp),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}

		if (playCount != null && playCount > 0) {
			val playsText = pluralStringResource(Res.plurals.count_plays, playCount, playCount)
			val timeText = duration?.toSummaryString()
			
			Text(
				text = if (timeText != null) "$playsText • $timeText" else playsText,
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.fillMaxWidth()
					.padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
