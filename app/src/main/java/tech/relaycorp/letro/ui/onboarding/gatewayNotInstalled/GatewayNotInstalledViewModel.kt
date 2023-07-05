package tech.relaycorp.letro.ui.onboarding.gatewayNotInstalled

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import tech.relaycorp.letro.repository.GatewayRepository
import javax.inject.Inject

@HiltViewModel
class GatewayNotInstalledViewModel @Inject constructor(
    private val gatewayRepository: GatewayRepository,
) : ViewModel() {

    fun onScreenResumed() {
        gatewayRepository.checkIfGatewayIsAvailable()
    }
}
