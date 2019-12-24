package com.voismart.crypto

import android.content.Context
import android.os.Build
import android.util.Log
import com.voismart.crypto.KeyStoreHelper.generateAsymmetricKeys
import com.voismart.crypto.KeyStoreHelper.generateSymmetricKey
import java.security.KeyStore

/**
 * crypto
 *
 * Created by Vincenzo Esposito on 26/11/19.
 * Copyright © 2019 VoiSmart S.r.l. All rights reserved.
 */
class Crypto {
    companion object {
        internal lateinit var alias: String
        internal val hasMarshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        private var forceAsymmetric = false

        /**
         * Initializes the Crypto library
         *
         * @param context -> Application Context
         * @param alias -> Alias used to store keys into AndroidKeyStore
         * @param forceAsymmetric -> Force asymmetric keys usage on Android 6.0+
         */
        @JvmStatic
        fun init(context: Context, alias: String, forceAsymmetric: Boolean = false) {
            this.alias = alias
            this.forceAsymmetric = forceAsymmetric

            initCrypto(context)
        }

        internal fun useSymmetric(): Boolean {
            return hasMarshmallow && !forceAsymmetric
        }

        private fun initCrypto(context: Context) {
            Log.i(Crypto::class.java.simpleName, "Initializing KeyStore")
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
            if (!keyStore.containsAlias(alias)) {
                createAndroidKeyStoreKeys(context)
            }
        }

        private fun createAndroidKeyStoreKeys(context: Context) {
            if (hasMarshmallow && !forceAsymmetric) {
                generateSymmetricKey()
            } else {
                generateAsymmetricKeys(context)
            }
        }

        internal const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }
}
