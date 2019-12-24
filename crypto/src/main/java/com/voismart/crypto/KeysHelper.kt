package com.voismart.crypto

import java.security.*

object KeysHelper {
    private val keyStore = KeyStore.getInstance(Crypto.ANDROID_KEY_STORE).apply { load(null) }

    @Throws(KeyStoreException::class, UnrecoverableEntryException::class)
    internal fun getKey(purpose: Purpose): Key? {
        return if (Crypto.useSymmetric()) {
            (keyStore.getEntry(Crypto.alias, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            when (purpose) {
                Purpose.DECRYPT -> keyStore.getKey(Crypto.alias, null) as PrivateKey
                Purpose.ENCRYPT -> keyStore.getCertificate(Crypto.alias).publicKey
            }
        }
    }

    enum class Purpose {
        DECRYPT,
        ENCRYPT
    }
}
