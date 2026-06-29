package com.example.data

import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * JVM unit tests for CredentialCipher's pure crypto core.
 *
 * Uses a plain JVM AES-256 key (no AndroidKeyStore) via the @VisibleForTesting
 * encryptWithKey / decryptWithKey entry points. No Android Keystore or Context required.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CredentialCipherTest {

    private fun generateAesKey(): SecretKey =
        KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    @Test
    fun encryptThenDecrypt_roundTrips_originalString() {
        val key = generateAesKey()
        val plaintext = "super-secret-token-abc123"
        val ciphertext = CredentialCipher.encryptWithKey(key, plaintext)
        val result = CredentialCipher.decryptWithKey(key, ciphertext)
        assertEquals(plaintext, result)
    }

    @Test
    fun emptyString_roundTrips_correctly() {
        val key = generateAesKey()
        val ciphertext = CredentialCipher.encryptWithKey(key, "")
        val result = CredentialCipher.decryptWithKey(key, ciphertext)
        assertEquals("", result)
    }

    @Test
    fun corruptedBase64_throws_CredentialDecryptionException() {
        val key = generateAesKey()
        assertThrows(CredentialDecryptionException::class.java) {
            CredentialCipher.decryptWithKey(key, "not-valid-base64!!!###")
        }
    }

    @Test
    fun tooShortCiphertext_throws_CredentialDecryptionException() {
        val key = generateAesKey()
        // Exactly 12 bytes encodes to IV only — no GCM ciphertext beyond IV
        val tooShort = Base64.encodeToString(ByteArray(12), Base64.NO_WRAP)
        assertThrows(CredentialDecryptionException::class.java) {
            CredentialCipher.decryptWithKey(key, tooShort)
        }
    }

    @Test
    fun twoEncryptions_sameKey_sameInput_produceDifferentCiphertext() {
        val key = generateAesKey()
        val plaintext = "same-plaintext"
        val cipher1 = CredentialCipher.encryptWithKey(key, plaintext)
        val cipher2 = CredentialCipher.encryptWithKey(key, plaintext)
        assertNotEquals("Random IV must produce different ciphertext each time", cipher1, cipher2)
    }
}
