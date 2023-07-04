package tech.relaycorp.letro.ui.onboarding.pair

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class PairViewModel @Inject constructor() : ViewModel() {

    private val _idUIFlow: MutableStateFlow<String> = MutableStateFlow("")
    val idUIFlow get() = _idUIFlow

    private val _aliasUIFlow: MutableStateFlow<String> = MutableStateFlow("")
    val aliasUIFlow get() = _aliasUIFlow

    fun onIdInput(id: String) {
        _idUIFlow.value = id
    }

    fun onAliasInput(alias: String) {
        _aliasUIFlow.value = alias
    }

    fun onRequestPairingClicked() {
        // TODO
    }
}
