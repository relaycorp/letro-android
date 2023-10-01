package tech.relaycorp.letro.notification.ui

sealed interface NotificationClickAction {
    object OpenContacts : NotificationClickAction
}
