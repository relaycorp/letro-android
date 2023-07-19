package tech.relaycorp.letro.ui.onboarding.pair

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import tech.relaycorp.letro.repository.AccountRepository
import javax.inject.Inject

@HiltViewModel
class PairViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _addressUIFlow: MutableStateFlow<String> = MutableStateFlow("")
    val idUIFlow get() = _addressUIFlow

    private val _aliasUIFlow: MutableStateFlow<String> = MutableStateFlow("")
    val aliasUIFlow get() = _aliasUIFlow

    fun onIdInput(id: String) {
        _addressUIFlow.value = id
    }

    fun onAliasInput(alias: String) {
        _aliasUIFlow.value = alias
    }

    fun onRequestPairingClicked() {
        accountRepository.startPairingWithContact(_addressUIFlow.value, _aliasUIFlow.value)
    }
}
