package paige.navic.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_content_unavailable
import org.jetbrains.compose.resources.stringResource
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Question

@Composable
fun ContentUnavailable(
	modifier: Modifier = Modifier.fillMaxSize(),
	label: String = stringResource(Res.string.info_content_unavailable),
	color: Color = MaterialTheme.colorScheme.onSurface
) {
	Column(
		modifier = modifier.fillMaxWidth().alpha(.6f),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
	) {
		Icon(
			imageVector = Icons.Outlined.Question,
			contentDescription = null,
			modifier = Modifier.width(110.dp).height(30.dp),
			tint = color
		)
		Text(
			label,
			style = MaterialTheme.typography.headlineMedium,
			color = color,
			textAlign = TextAlign.Center,
			modifier = Modifier.widthIn(max = 400.dp)
		)
	}
}
