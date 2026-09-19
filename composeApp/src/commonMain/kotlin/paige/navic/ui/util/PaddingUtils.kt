package paige.navic.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.minus

fun PaddingValues.withoutTop() = this.minus(PaddingValues(top = this.calculateTopPadding()))

fun PaddingValues.withoutBottom() = this.minus(PaddingValues(bottom = this.calculateBottomPadding()))
