package paige.navic.ui.screens.stats.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import paige.navic.ui.theme.defaultFont

@Composable
fun StatSummaryCard(
	modifier: Modifier = Modifier,
	label: String,
	value: String
) {
	Surface(
		modifier = modifier,
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
		shape = MaterialTheme.shapes.medium
	) {
		Column(Modifier.padding(16.dp)) {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				text = value,
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Bold,
				fontFamily = defaultFont(round = 100f)
			)
		}
	}
}
