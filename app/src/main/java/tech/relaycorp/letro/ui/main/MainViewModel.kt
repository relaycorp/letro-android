package tech.relaycorp.letro.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.repository.AccountRepository
import tech.relaycorp.letro.repository.GatewayRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    gatewayRepository: GatewayRepository,
    accountRepository: AccountRepository,
) : ViewModel() {

    private val _firstNavigationUIModelFlow: MutableStateFlow<FirstNavigationUIModel> =
        MutableStateFlow(FirstNavigationUIModel.Splash)
    val firstNavigationUIModelFlow: StateFlow<FirstNavigationUIModel> get() = _firstNavigationUIModelFlow

    private val _mainUIStateFlow: MutableStateFlow<MainUIState> = MutableStateFlow(MainUIState())
    val mainUIStateFlow: StateFlow<MainUIState> get() = _mainUIStateFlow

    init {
        viewModelScope.launch {
            gatewayRepository.isGatewayAvailable.collect { gatewayAvailability ->
                if (gatewayAvailability == false) {
                    _firstNavigationUIModelFlow.emit(FirstNavigationUIModel.NoGateway)
                }
            }
        }

        viewModelScope.launch {
            gatewayRepository.isGatewayFullySetup.collect { gatewaySetup ->
                if (gatewaySetup) {
                    _firstNavigationUIModelFlow.emit(FirstNavigationUIModel.AccountCreation)
                }
            }
        }

        viewModelScope.launch {
            accountRepository.currentAccountDataFlow.collect {
                it?.let { dataModel ->
                    _mainUIStateFlow.emit(
                        MainUIState(
                            address = dataModel.address,
                            isAccountCreated = dataModel.isCreationConfirmed,
                        ),
                    )
                }
            }
        }
    }
}
