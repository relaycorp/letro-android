package tech.relaycorp.letro.messages.onboarding

import tech.relaycorp.letro.storage.Preferences
import javax.inject.Inject

interface ConversationsOnboardingManager {
    fun isOnboardingMessageWasShown(userId: String): Boolean
    fun saveOnboardingMessageShown(userId: String)
}

class ConversationsOnboardingManagerImpl @Inject constructor(
    private val preferences: Preferences,
) : ConversationsOnboardingManager {

    override fun isOnboardingMessageWasShown(userId: String): Boolean {
        return preferences.getBoolean(getPreferenceKey(userId), false)
    }

    override fun saveOnboardingMessageShown(userId: String) {
        preferences.putBoolean(getPreferenceKey(userId), true)
    }

    private fun getPreferenceKey(userId: String) =
        "$KEY_ONBOARDING_SHOWN_PREFIX$userId"

    private companion object {
        private const val KEY_ONBOARDING_SHOWN_PREFIX = "is_messages_safe_onboarding_shown_"
    }
}
