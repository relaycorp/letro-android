package tech.relaycorp.letro.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tech.relaycorp.letro.repository.AccountRepository
import tech.relaycorp.letro.repository.ContactRepository
import tech.relaycorp.letro.repository.GatewayRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    gatewayRepository: GatewayRepository,
    accountRepository: AccountRepository,
    contactRepository: ContactRepository,
) : ViewModel() {

    private val _firstNavigationUIModelFlow: MutableStateFlow<InitialAppNavigationUIModel> =
        MutableStateFlow(InitialAppNavigationUIModel.Splash)
    val firstNavigationUIModelFlow: StateFlow<InitialAppNavigationUIModel> get() = _firstNavigationUIModelFlow

    private val _mainUIStateFlow: MutableStateFlow<MainUIState> = MutableStateFlow(MainUIState())
    val mainUIStateFlow: StateFlow<MainUIState> get() = _mainUIStateFlow

    private val _replayInitialAppNavigation: MutableSharedFlow<Unit> = MutableSharedFlow()
    val replayInitialAppNavigation: SharedFlow<Unit> get() = _replayInitialAppNavigation

    init {
        viewModelScope.launch {
            gatewayRepository.isGatewayAvailable.collect { gatewayAvailability ->
                if (gatewayAvailability == false) {
                    _firstNavigationUIModelFlow.emit(InitialAppNavigationUIModel.NoGateway)
                }
            }
        }

        viewModelScope.launch {
            combine(
                gatewayRepository.isGatewayFullySetup,
                accountRepository.currentAccountDataFlow,
                contactRepository.pairedContactsExist,
            ) { gatewaySetup, accountDataModel, pairedContactsExist ->
                if (gatewaySetup) {
                    if (accountDataModel == null) {
                        _firstNavigationUIModelFlow.emit(InitialAppNavigationUIModel.AccountCreation)
                    } else {
                        if (accountDataModel.isCreationConfirmed) {
                            if (pairedContactsExist) {
                                _firstNavigationUIModelFlow.emit(InitialAppNavigationUIModel.Conversations)
                            } else {
                                _firstNavigationUIModelFlow.emit(InitialAppNavigationUIModel.AccountCreationConfirmed)
                            }
                        } else {
                            _firstNavigationUIModelFlow.emit(InitialAppNavigationUIModel.WaitingForAccountCreationConfirmation)
                        }
                    }
                }
            }.collect()
        }

        viewModelScope.launch {
            accountRepository.currentAccountDataFlow.collect {
                it?.let { dataModel ->
                    _mainUIStateFlow.emit(
                        MainUIState(
                            veraId = dataModel.veraId,
                            isAccountCreated = dataModel.isCreationConfirmed,
                        ),
                    )
                }
            }
        }
    }

    fun onGotItClickedAfterPairingRequestSent() {
        viewModelScope.launch {
            _replayInitialAppNavigation.emit(Unit)
        }
    }
}
