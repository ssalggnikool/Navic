package paige.navic.ui.screens.chat.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zt64.subsonic.api.model.ChatMessage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import paige.navic.domain.manager.SessionManager
import paige.navic.ui.core.UiState
import paige.navic.util.Logger
import kotlin.time.Duration.Companion.seconds

class ChatViewModel(
	private val sessionManager: SessionManager
) : ViewModel() {
	val state: StateFlow<UiState<ImmutableList<ChatMessage>>>
		field = MutableStateFlow<UiState<ImmutableList<ChatMessage>>>(UiState.Loading<ImmutableList<ChatMessage>>())

	val isSendingMessage: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val events: SharedFlow<Event>
		field = MutableSharedFlow<Event>()

	val inputState = TextFieldState()

	init {
		viewModelScope.launch {
			while (isActive) {
				refreshMessages()
				delay(10.seconds)
			}
		}
	}

	fun refreshMessages() {
		viewModelScope.launch {
			println("refresh")
			state.value = UiState.Loading(state.value.data)
			try {
				state.value = UiState.Success(sessionManager.api.getChatMessages().toPersistentList())
			} catch (ex: Exception) {
				state.value = UiState.Error(ex, state.value.data)
				Logger.e("ChatViewModel", "could not refresh messages", ex)
			}
		}
	}

	fun sendMessage() {
		viewModelScope.launch {
			isSendingMessage.value = true
			try {
				sessionManager.api.addChatMessage(inputState.text.toString())
				refreshMessages()
			} catch (ex: Exception) {
				Logger.e("ChatViewModel", "could not send message", ex)
			}
			isSendingMessage.value = false
			inputState.clearText()
			events.emit(Event.ScrollDown)
		}
	}

	sealed class Event {
		object ScrollDown : Event()
	}
}
