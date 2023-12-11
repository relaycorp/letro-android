package tech.relaycorp.letro.contact

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.utils.models.contact.createContact
import tech.relaycorp.letro.utils.models.contact.createContactsViewModel

class ContactsViewModelTest {

    @Test
    fun `Test that manage contacts bottom sheet is opened after click on action button`() {
        val contactsViewModel = createContactsViewModel()
        contactsViewModel.contactActionsBottomSheetState.value.isShown shouldBe false

        contactsViewModel.onActionsButtonClick(createContact())
        contactsViewModel.contactActionsBottomSheetState.value.isShown shouldBe true
    }

    @Test
    fun `Test that contact manage bottom sheet is being dismissed`() {
        val contactsViewModel = createContactsViewModel()
        contactsViewModel.contactActionsBottomSheetState.value.isShown shouldBe false

        contactsViewModel.onActionsButtonClick(createContact())
        contactsViewModel.contactActionsBottomSheetState.value.isShown shouldBe true

        contactsViewModel.onActionsBottomSheetDismissed()
        contactsViewModel.contactActionsBottomSheetState.value.isShown shouldBe false
    }
}
