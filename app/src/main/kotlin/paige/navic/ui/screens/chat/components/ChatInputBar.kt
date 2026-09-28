package paige.navic.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import paige.navic.ui.theme.defaultFont

@Composable
fun ChatInputBar(
	modifier: Modifier = Modifier,
	state: TextFieldState,
	onSendAction: () -> Unit,
	enabled: Boolean = true
) {
	BasicTextField(
		state = state,
		enabled = enabled,
		modifier = modifier
			.height(56.dp)
			.background(MaterialTheme.colorScheme.surfaceContainer, ContinuousCapsule),
		lineLimits = TextFieldLineLimits.SingleLine,
		keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
		onKeyboardAction = { onSendAction() },
		textStyle = TextStyle(
			color = MaterialTheme.colorScheme.onSurface,
			fontFamily = defaultFont()
		),
		cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
		decorator = { innerTextField ->
			Box(
				modifier = Modifier.padding(horizontal = 16.dp),
				contentAlignment = Alignment.CenterStart
			) {
				innerTextField()
			}
		}
	)
}
