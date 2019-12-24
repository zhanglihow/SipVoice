package com.voismart.crypto

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.util.*
import javax.crypto.KeyGenerator
import javax.security.auth.x500.X500Principal

object KeyStoreHelper {

    /**
     * Could potentially throw
     * InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchAlgorithmException
     * but those parameters are static, thus valid
     */
    internal fun generateAsymmetricKeys(context: Context) {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA", Crypto.ANDROID_KEY_STORE)

        if (Crypto.hasMarshmallow) {
            initGeneratorWithKeyGenParameterSpec(keyPairGenerator)
        } else {
            initGeneratorWithKeyPairGeneratorSpec(keyPairGenerator, context)
        }

        keyPairGenerator.generateKeyPair()
    }

    /**
     * Could potentially throw
     * InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchAlgorithmException
     * but those parameters are static, thus valid
     */
    @TargetApi(Build.VERSION_CODES.M)
    internal fun generateSymmetricKey() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, Crypto.ANDROID_KEY_STORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(Crypto.alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun initGeneratorWithKeyGenParameterSpec(generator: KeyPairGenerator) {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(Crypto.alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .build()
        generator.initialize(keyGenParameterSpec)
    }

    @SuppressWarnings( "deprecation" )
    private fun initGeneratorWithKeyPairGeneratorSpec(keyPairGenerator: KeyPairGenerator, context: Context) {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, 50) // defaults to 50 years certificate expiration

        val keyPairGeneratorSpec = android.security.KeyPairGeneratorSpec.Builder(context)
            .setAlias(Crypto.alias)
            .setSubject(X500Principal("CN=${Crypto.alias} CA Certificate"))
            .setSerialNumber(BigInteger.ONE)
            .setStartDate(startDate.time)
            .setEndDate(endDate.time)
            .build()

        keyPairGenerator.initialize(keyPairGeneratorSpec)
    }
}
