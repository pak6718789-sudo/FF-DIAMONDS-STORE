package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 12000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    data class HashedPassword(
        val hashBase64: String,
        val saltBase64: String
    )

    fun hashPassword(password: String): HashedPassword {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = pbkdf2(password.toCharArray(), salt)
        return HashedPassword(
            hashBase64 = toHex(hash),
            saltBase64 = toHex(salt)
        )
    }

    fun verifyPassword(password: String, expectedHash: String, saltHex: String): Boolean {
        return try {
            val salt = fromHex(saltHex)
            val expected = fromHex(expectedHash)
            val actual = pbkdf2(password.toCharArray(), salt)
            MessageDigest.isEqual(actual, expected)
        } catch (_: Exception) {
            false
        }
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun toHex(bytes: ByteArray): String =
        bytes.joinToString(separator = "") { "%02x".format(it) }

    private fun fromHex(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Invalid hex string" }
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
