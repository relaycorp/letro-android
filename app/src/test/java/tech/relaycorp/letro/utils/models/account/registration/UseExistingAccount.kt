package tech.relaycorp.letro.utils.models.account.registration

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import tech.relaycorp.letro.account.registration.UseExistingAccountViewModel
import tech.relaycorp.letro.account.registration.storage.RegistrationRepository
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import tech.relaycorp.letro.utils.models.utils.dispatchers

@OptIn(ExperimentalCoroutinesApi::class)
fun createUseExistingAccountViewModel(
    registrationRepository: RegistrationRepository = createRegistrationRepository(),
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    dispatchers: Dispatchers = dispatchers(),
) = UseExistingAccountViewModel(registrationRepository, savedStateHandle, dispatchers)
