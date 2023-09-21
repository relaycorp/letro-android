package tech.relaycorp.letro.ui.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import tech.relaycorp.letro.R
import javax.inject.Inject

interface AwalaInitializationStringsProvider {
    val awalaInitializationAmusingTexts: Array<String>
}

class AwalaInitializationStringsProviderImpl @Inject constructor(
    @ActivityContext private val context: Context,
) : AwalaInitializationStringsProvider {

    override val awalaInitializationAmusingTexts: Array<String>
        get() = arrayOf(
            context.getString(R.string.filling_the_kettle),
            context.getString(R.string.starting_the_kettle),
            context.getString(R.string.putting_the_tea_bag),
            context.getString(R.string.putting_the_hot_water),
            context.getString(R.string.letting_the_tea_brew),
            context.getString(R.string.removing_the_tea_bag),
            context.getString(R.string.drinking_the_tea),
        )
}
