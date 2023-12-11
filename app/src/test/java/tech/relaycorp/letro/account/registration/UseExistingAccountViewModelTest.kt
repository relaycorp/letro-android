package tech.relaycorp.letro.account.registration

import androidx.lifecycle.SavedStateHandle
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.ext.encodeToUTF
import tech.relaycorp.letro.utils.models.account.registration.createUseExistingAccountViewModel

class UseExistingAccountViewModelTest {

    @Test
    fun `Test decoding of arguments in SavedStateHandle`() {
        val domainDecoded = "domain"
        val endpointDecoded = "endpoint"
        val tokenDecoded = "qwr901-e412-ads"
        val arguments = SavedStateHandle(
            mapOf(
                Route.UseExistingAccount.DOMAIN_ENCODED to domainDecoded.encodeToUTF(),
                Route.UseExistingAccount.AWALA_ENDPOINT_ENCODED to endpointDecoded.encodeToUTF(),
                Route.UseExistingAccount.TOKEN_ENCODED to tokenDecoded.encodeToUTF(),
            ),
        )
        val viewModel = createUseExistingAccountViewModel(
            registrationRepository = mockk(relaxed = true),
            savedStateHandle = arguments,
        )

        with(viewModel.uiState.value) {
            domain shouldBe domainDecoded
            awalaEndpoint shouldBe endpointDecoded
            token shouldBe tokenDecoded
        }
    }

    @Test
    fun `Test that proceed button is not enabled if it's incorrect format`() {
        val domainDecoded = "correct.domain"
        val tokenDecoded = "qwr901-e412-ads"
        val viewModel = createUseExistingAccountViewModel(
            registrationRepository = mockk(relaxed = true),
            savedStateHandle = SavedStateHandle(
                mapOf(
                    Route.UseExistingAccount.DOMAIN_ENCODED to domainDecoded.encodeToUTF(),
                    Route.UseExistingAccount.TOKEN_ENCODED to tokenDecoded.encodeToUTF(),
                ),
            ),
        )

        with(viewModel.uiState.value) {
            domain shouldBe domainDecoded
            token shouldBe tokenDecoded
            isProceedButtonEnabled shouldBe true
        }

        viewModel.onDomainInput("incorrectdomain")
        viewModel.uiState.value.isProceedButtonEnabled shouldBe false

        viewModel.onDomainInput("incorrect domain")
        viewModel.uiState.value.isProceedButtonEnabled shouldBe false

        viewModel.onDomainInput("incorrect. domain")
        viewModel.uiState.value.isProceedButtonEnabled shouldBe false

        viewModel.onDomainInput("correct.domain")
        viewModel.uiState.value.isProceedButtonEnabled shouldBe true
    }

    @Test
    fun `Test that proceed button is not enabled if screen was opened without arguments`() {
        val viewModel = createUseExistingAccountViewModel(
            registrationRepository = mockk(relaxed = true),
            savedStateHandle = SavedStateHandle(),
        )

        with(viewModel.uiState.value) {
            domain shouldBe ""
            token shouldBe ""
            awalaEndpoint shouldBe ""
            isProceedButtonEnabled shouldBe false
        }
    }
}
