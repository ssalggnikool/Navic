package paige.navic.ui.screen.login.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import paige.navic.domain.server.AuthMethod

class LoginViewModel : ViewModel() {
	val authMethod: StateFlow<AuthMethod?>
		field = MutableStateFlow<AuthMethod?>(null)

	fun selectAuthMethod(method: AuthMethod?) {
		authMethod.value = method
	}
}
