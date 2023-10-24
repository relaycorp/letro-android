package tech.relaycorp.letro.storage.encryption

import android.content.Context
import android.os.Build
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.SecureRandom
import javax.inject.Inject

interface DatabasePassphrase {
    fun getOrCreate(): ByteArray
}

class DatabasePassphraseImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val masterKey: MasterKey,
) : DatabasePassphrase {

    override fun getOrCreate(): ByteArray {
        val passphraseFile = File(context.filesDir, PASSPHRASE_FILE_PATH)
        val passphraseEncryptedFile = EncryptedFile.Builder(
            context,
            passphraseFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        )
            // Set a explicit preference name to avoid cryptic `AEADBadTagException`s when multiple
            // `MasterKey`s are used by the app.
            .setKeysetPrefName(ENCRYPTED_FILE_PREF_NAME)
            .build()
        return if (passphraseFile.exists()) {
            passphraseEncryptedFile.openFileInput().use {
                it.readBytes()
            }
        } else {
            generatePassphrase().also { passphrase ->
                passphraseEncryptedFile.openFileOutput().use { it.write(passphrase) }
            }
        }
    }

    private fun generatePassphrase(): ByteArray = ByteArray(PASSPHRASE_LENGTH).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SecureRandom.getInstanceStrong().nextBytes(this)
        } else {
            SecureRandom().nextBytes(this)
        }
    }

    private companion object {
        private const val PASSPHRASE_LENGTH = 32
        private const val PASSPHRASE_FILE_PATH = "letro-db-passphrase"
        private const val ENCRYPTED_FILE_PREF_NAME = "db-keyset"
    }
}
