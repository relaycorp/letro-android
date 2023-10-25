package tech.relaycorp.letro.utils.crypto

import android.content.Context
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface MasterKeyProvider {
    val masterKey: MasterKey
}

class MasterKeyProviderImpl @Inject constructor(
    @ApplicationContext context: Context,
) : MasterKeyProvider {

    override val masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
}

private const val MASTER_KEY_ALIAS = "_letro_master_key_"
