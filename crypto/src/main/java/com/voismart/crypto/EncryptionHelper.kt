package com.voismart.crypto

import android.util.Base64
import com.voismart.crypto.KeysHelper.getKey
import java.security.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec

@SuppressWarnings("unused")
class EncryptionHelper private constructor() {

    @Throws(NoSuchPaddingException::class,
        InvalidKeyException::class,
        KeyStoreException::class,
        UnrecoverableEntryException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class)
    fun encrypt(data: String?): String? {
        var cypherText: String? = ""
        data?.let {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, getKey(KeysHelper.Purpose.ENCRYPT))
            if (Crypto.useSymmetric()) {
                val ivString = Base64.encodeToString(cipher.iv, Base64.DEFAULT)
                cypherText = ivString + IV_SEPARATOR
            }
            val bytes = cipher.doFinal(data.toByteArray())
            cypherText += Base64.encodeToString(bytes, Base64.DEFAULT)
            return cypherText
        }
        return null
    }

    @Throws(NoSuchPaddingException::class,
        InvalidKeyException::class,
        KeyStoreException::class,
        UnrecoverableEntryException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class)
    fun decrypt(data: String?): String? {
        data?.let {
            val cipher = Cipher.getInstance(transformation)
            var cypherText = data
            if (Crypto.useSymmetric()) {
                val split = splitTextAndIv(data)
                if (split != null) {
                    cypherText = split.first
                    cipher.init(
                        Cipher.DECRYPT_MODE,
                        getKey(KeysHelper.Purpose.DECRYPT),
                        split.second
                    )
                } else return null
            } else cipher.init(Cipher.DECRYPT_MODE, getKey(KeysHelper.Purpose.DECRYPT))
            val encryptedData = Base64.decode(cypherText, Base64.DEFAULT)
            val decodedData = cipher.doFinal(encryptedData)
            return String(decodedData)
        }
        return null
    }

    private fun splitTextAndIv(cypherText: String?): Pair<String, IvParameterSpec>? {
        cypherText?.let {
            val split = cypherText.split(IV_SEPARATOR.toRegex())
            if (split.size == 2) {
                val ivString = split[0]
                val encodedString = split[1]
                val ivSpec = IvParameterSpec(Base64.decode(ivString, Base64.DEFAULT))
                return Pair(encodedString, ivSpec)
            }
        }
        return null
    }

    companion object {
        private var instance: EncryptionHelper? = null
        private var transformation = getTransformation()
        private const val IV_SEPARATOR = "]"

        fun getInstance(): EncryptionHelper {
            if (instance == null) {
                instance = EncryptionHelper()
            }
            return instance!!
        }

        private fun getTransformation(): String {
            return if (Crypto.useSymmetric()) {
                Transformation.SYMMETRIC.type
            } else {
                Transformation.ASYMMETRIC.type
            }
        }
    }
}
