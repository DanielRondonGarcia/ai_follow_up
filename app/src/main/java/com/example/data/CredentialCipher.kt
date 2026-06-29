package com.example.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import androidx.annotation.VisibleForTesting
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CredentialDecryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Abstraction over credential encryption. Production impl is Keystore-backed;
 *  tests inject a deterministic fake so Room/ViewModel tests need no Keystore. */
interface CredentialEncryptor {
    fun encrypt(plaintext: String): String
    fun decrypt(ciphertext: String): String
}

class KeystoreCredentialEncryptor(private val context: Context) : CredentialEncryptor {
    override fun encrypt(plaintext: String): String = CredentialCipher.encrypt(context, plaintext)
    override fun decrypt(ciphertext: String): String = CredentialCipher.decrypt(context, ciphertext)
}

object CredentialCipher {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "credential_cipher_key_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_LENGTH_BYTES = 12
    private const val TAG_LENGTH_BITS = 128
    private const val KEY_SIZE_BITS = 256

    fun encrypt(context: Context, plaintext: String): String =
        encryptWithKey(getOrCreateKey(), plaintext)

    fun decrypt(context: Context, ciphertext: String): String =
        decryptWithKey(getExistingKeyOrThrow(), ciphertext)

    @VisibleForTesting
    internal fun encryptWithKey(key: SecretKey, plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)            // GCM generates a random 12-byte IV
        val iv = cipher.iv
        val ct = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + ct.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ct, 0, combined, iv.size, ct.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    @VisibleForTesting
    internal fun decryptWithKey(key: SecretKey, ciphertext: String): String {
        try {
            val combined = Base64.decode(ciphertext, Base64.NO_WRAP)
            if (combined.size <= IV_LENGTH_BYTES)
                throw CredentialDecryptionException("Ciphertext shorter than IV")
            val iv = combined.copyOfRange(0, IV_LENGTH_BYTES)
            val ct = combined.copyOfRange(IV_LENGTH_BYTES, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
            return String(cipher.doFinal(ct), Charsets.UTF_8)
        } catch (e: CredentialDecryptionException) {
            throw e
        } catch (e: Exception) {
            throw CredentialDecryptionException("Failed to decrypt credential", e)
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        return generateKey()
    }

    private fun getExistingKeyOrThrow(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val entry = ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            ?: throw CredentialDecryptionException("Keystore key '$KEY_ALIAS' not found")
        return entry.secretKey
    }

    private fun generateKey(): SecretKey {
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(true)
        return try {
            generate(builder.setIsStrongBoxBacked(true).build())
        } catch (e: StrongBoxUnavailableException) {
            generate(builder.setIsStrongBoxBacked(false).build())
        }
    }

    private fun generate(spec: KeyGenParameterSpec): SecretKey {
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        kg.init(spec)
        return kg.generateKey()
    }
}
