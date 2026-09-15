package paige.navic.ui.screens.stats.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import paige.navic.ui.theme.defaultFont

@Composable
fun StatSummaryCard(
	modifier: Modifier = Modifier,
	horizontal: Boolean = false,
	color: Color,
	contentColor: Color,
	icon: @Composable () -> Unit,
	label: String,
	value: String
) {
	val content = @Composable {
		icon()
		Spacer(if (horizontal) Modifier.width(8.dp) else Modifier.height(8.dp))
		Column {
			Text(
				text = value,
				style = MaterialTheme.typography.headlineMedium,
				color = contentColor,
				fontWeight = FontWeight.SemiBold,
				fontFamily = defaultFont(round = 100f)
			)
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
				color = contentColor,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
	Surface(
		modifier = modifier,
		color = color,
		shape = MaterialTheme.shapes.largeIncreased
	) {
		if (horizontal) {
			Row(
				modifier = Modifier.padding(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				content()
			}
		} else {
			Column(Modifier.padding(12.dp)) {
				content()
			}
		}
	}
}
