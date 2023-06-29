package tech.realycorp.letro.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.realycorp.letro.data.GatewayAvailabilityDataModel
import tech.realycorp.letro.repository.GatewayRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    gatewayRepository: GatewayRepository,
) : ViewModel() {

    private val _firstNavigationUIModelFlow: MutableStateFlow<FirstNavigationUIModel> =
        MutableStateFlow(FirstNavigationUIModel.Splash)
    val firstNavigationUIModelFlow: StateFlow<FirstNavigationUIModel> get() = _firstNavigationUIModelFlow

    init {
        viewModelScope.launch {
            gatewayRepository.gatewayAvailabilityDataModel.collect { gatewayAvailability ->
                when (gatewayAvailability) {
                    GatewayAvailabilityDataModel.Available -> _firstNavigationUIModelFlow.emit(FirstNavigationUIModel.AccountCreation)
                    GatewayAvailabilityDataModel.Unavailable -> _firstNavigationUIModelFlow.emit(FirstNavigationUIModel.NoGateway)
                    else -> {}
                }
            }
        }
    }
}
