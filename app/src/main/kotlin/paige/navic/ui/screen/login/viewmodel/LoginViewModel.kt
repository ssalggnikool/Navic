package paige.navic.ui.screen.login.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import paige.navic.domain.server.AuthMethod
import paige.navic.domain.server.ServerType

class LoginViewModel : ViewModel() {
	val authMethod: StateFlow<AuthMethod?>
		field = MutableStateFlow<AuthMethod?>(null)

	val serverType: StateFlow<ServerType?>
		field = MutableStateFlow<ServerType?>(null)

	fun selectAuthMethod(method: AuthMethod?) {
		authMethod.value = method
	}

	fun selectServerType(type: ServerType?) {
		serverType.value = type
	}
}
