package com.cs446g15.app.util
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

class CryptoUtils {
    fun generateKeyPair(): Pair<PrivateKey, PublicKey> {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.genKeyPair()
        return keyPair.private to keyPair.public
    }

    fun signData(data: ByteArray, privateKey: PrivateKey): ByteArray {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }

    fun verifySignature(data: ByteArray, signature: ByteArray, publicKey: PublicKey): Boolean {
        val verify = Signature.getInstance("SHA256withRSA")
        verify.initVerify(publicKey)
        verify.update(data)
        return verify.verify(signature)
    }
}