package tech.relaycorp.letro.ui.conversations.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor() : ViewModel() {

    private val _messagesUIStateFlow: MutableStateFlow<MessagesUIStateModel> = MutableStateFlow(MessagesUIStateModel())
    val messagesUIStateFlow: MutableStateFlow<MessagesUIStateModel> get() = _messagesUIStateFlow

    init {
        viewModelScope.launch {
            // TODO
        }
    }
}
