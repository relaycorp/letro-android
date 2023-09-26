package tech.relaycorp.letro.notification

sealed interface NotificationClickAction {
    object OpenContacts : NotificationClickAction
}
