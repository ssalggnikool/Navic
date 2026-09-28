package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class RootViewModel : ViewModel() {
	val events: SharedFlow<Event>
		field = MutableSharedFlow<Event>()

	fun requestScrollToTop() {
		viewModelScope.launch {
			events.emit(Event.ScrollToTop)
		}
	}

	sealed class Event {
		object ScrollToTop : Event()
	}
}
