package tech.relaycorp.letro.utils.crypto

import android.content.Context
import androidx.security.crypto.MasterKey

object MasterKeyProvider {

    lateinit var masterKey: MasterKey
        private set

    fun init(context: Context) {
        this.masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
}

private const val MASTER_KEY_ALIAS = "_letro_master_key_"
