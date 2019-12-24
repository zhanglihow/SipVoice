package com.voismart.crypto

/**
 * Crypto
 *
 * Created by Vincenzo Esposito on 26/11/19.
 * Copyright © 2018 VoiSmart S.r.l. All rights reserved.
 */
enum class Transformation(val type: String) {
    ASYMMETRIC("RSA/ECB/PKCS1Padding"),
    SYMMETRIC("AES/CBC/PKCS7Padding")
}