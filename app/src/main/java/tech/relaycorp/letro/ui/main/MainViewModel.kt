package tech.relaycorp.letro.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.GatewayAvailabilityDataModel
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

    private val _accountUsernameFlow: MutableStateFlow<String> = MutableStateFlow("")
    val accountUsernameFlow: StateFlow<String> get() = _accountUsernameFlow

    private val _continueAppFlowAfterAccountCreationConfirmed: MutableSharedFlow<Unit> = MutableSharedFlow()
    val continueAppFlowAfterAccountCreationConfirmed: SharedFlow<Unit> get() = _continueAppFlowAfterAccountCreationConfirmed

    init {
        viewModelScope.launch {
            gatewayRepository.gatewayAvailabilityDataModel.collect { gatewayAvailability ->
                when (gatewayAvailability) {
                    GatewayAvailabilityDataModel.Available -> _firstNavigationUIModelFlow.emit(
                        FirstNavigationUIModel.AccountCreation,
                    )

                    GatewayAvailabilityDataModel.Unavailable -> _firstNavigationUIModelFlow.emit(
                        FirstNavigationUIModel.NoGateway,
                    )

                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            accountRepository.currentAccountDataFlow.collect {
                it?.let { dataModel ->
                    _accountUsernameFlow.emit(dataModel.address)
                    if (dataModel.isCreationConfirmed) {
                        _continueAppFlowAfterAccountCreationConfirmed.emit(Unit)
                    }
                }
            }
        }
    }
}
