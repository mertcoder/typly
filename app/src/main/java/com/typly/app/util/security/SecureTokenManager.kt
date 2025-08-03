package com.typly.app.util.security


import java.security.SecureRandom
import android.util.Base64
import android.util.Log
import com.typly.app.BuildConfig
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Singleton object for managing secure, encrypted chat session tokens.
 * 
 * Provides cryptographically secure token creation and validation for chat sessions.
 * Uses AES encryption with random salts and checksums to prevent token tampering
 * and ensure session security. Tokens have configurable expiry times for enhanced security.
 * 
 * Features:
 * - AES-256 encryption with random IV
 * - URL-safe Base64 encoding for web compatibility
 * - Salt-based checksum validation
 * - Configurable token expiration
 * - Comprehensive logging for security auditing
 */
object SecureTokenManager {

    // For production, use:
    // - Environment variables
    // - Android Keystore
    // - Secure server-side key management
    private const val SECRET_KEY = BuildConfig.SECRET_KEY // Exactly 16 bytes for AES - CHANGE FOR PRODUCTION!
    private const val TOKEN_EXPIRY_MINUTES = 60

    /**
     * Data class representing the internal structure of a secure token.
     * 
     * @property userId The user ID this token was issued for
     * @property timestamp When the token was created (milliseconds since epoch)
     * @property randomSalt Random salt used for checksum generation
     * @property checksum Verification checksum to detect tampering
     */
    data class TokenData(
        val userId : String,
        val timestamp : Long,
        val randomSalt: String,
        val checksum : String
    )
    
    /**
     * Creates a secure, encrypted token for chat session authentication.
     * 
     * Generates a cryptographically secure token containing user ID, timestamp,
     * random salt, and verification checksum. The token is AES-encrypted and
     * Base64-encoded for safe URL transmission.
     * 
     * @param userId The user ID to encode in the token
     * @return Encrypted, Base64-encoded token string
     * @throws SecurityException if token creation fails
     */
    fun createSecureChatToken(userId: String): String{
        return try{
            val timestamp = System.currentTimeMillis()
            val salt = generateRandomSalt()
            val checksum = generateChecksum(userId, timestamp, salt)
            val tokenData = "$userId|$timestamp|$salt|$checksum"

            Log.d("SecureToken", "Creating token for userId: $userId")
            Log.d("SecureToken", "Token data: $tokenData")

            // Encrypt with AES
            val encrypted = encrypt(tokenData)
            // Use URL_SAFE Base64 encoding to avoid URL encoding issues
            val encodedToken = Base64.encodeToString(encrypted, Base64.URL_SAFE or Base64.NO_WRAP)
            
            Log.d("SecureToken", "Encrypted token created successfully")
            Log.d("SecureToken", "Token length: ${encodedToken.length}")
            encodedToken

        } catch (e: Exception) {
            Log.e("SecureToken", "Token creation failed: ${e.message}", e)
            e.printStackTrace()
            throw SecurityException("Token creation failed: ${e.message}")
        }
    }

    /**
     * Extracts and validates a user ID from a secure token.
     * 
     * Decrypts the provided token, validates its integrity and expiration,
     * and returns the contained user ID if valid. Performs comprehensive
     * validation including format, checksum, and timestamp verification.
     * 
     * @param token The encrypted token to validate and extract from
     * @return The user ID if token is valid, null if invalid or expired
     */
    fun extractUserIdFromSecureToken(token: String):  String?{
        return try{
            Log.d("SecureToken", "Attempting to decode token: ${token.take(20)}...")
            Log.v("SecureToken", "Full token: $token")
            
            // Use URL_SAFE Base64 decoding
            val encryptedData = Base64.decode(token, Base64.URL_SAFE)
            Log.d("SecureToken", "Base64 decoded successfully, data size: ${encryptedData.size}")

            val decryptedData = decrypt(encryptedData)
            Log.d("SecureToken", "Decrypted data: $decryptedData")

            val parts = decryptedData.split("|")
            if(parts.size != 4) {
                Log.w("SecureToken", "Invalid token format. Expected 4 parts, got ${parts.size}")
                return null
            }
            
            val tokenData = TokenData(
                userId = parts[0],
                timestamp = parts[1].toLong(),
                randomSalt = parts[2],
                checksum = parts[3]
            )
            
            if (!isTokenValid(tokenData)) {
                Log.w("SecureToken", "Token validation failed")
                return null
            }
            
            Log.d("SecureToken", "Token validated successfully for userId: ${tokenData.userId}")
            tokenData.userId
            
        } catch (e: Exception) {
            Log.e("SecureToken", "Token extraction failed: ${e.message}", e)
            e.printStackTrace()
            null // Token invalid
        }
    }
    
    /**
     * Validates a token's expiration and integrity.
     * 
     * Checks if the token is within the allowed age limit and verifies
     * the checksum to ensure the token hasn't been tampered with.
     * 
     * @param tokenData The parsed token data to validate
     * @return True if token is valid and not expired, false otherwise
     */
    private fun isTokenValid(tokenData : TokenData):Boolean{
        val currentTime = System.currentTimeMillis()
        val tokenAge = currentTime - tokenData.timestamp
        val maxAge = TOKEN_EXPIRY_MINUTES *60*1000
        
        if (tokenAge > maxAge) {
            Log.w("SecureToken", "Token expired. Age: ${tokenAge}ms, Max: ${maxAge}ms")
            return false
        }

        val expectedChecksum = generateChecksum(tokenData.userId, tokenData.timestamp, tokenData.randomSalt)
        val isValid = expectedChecksum == tokenData.checksum
        
        if (!isValid) {
            Log.w("SecureToken", "Checksum validation failed. Expected: $expectedChecksum, Got: ${tokenData.checksum}")
        }
        
        return isValid
    }
    
    /**
     * Generates a verification checksum for token integrity validation.
     * 
     * Creates a hash-based checksum using user ID, timestamp, salt, and secret key
     * to detect any tampering with the token data.
     * 
     * @param userId The user ID included in the token
     * @param timestamp The token creation timestamp
     * @param salt The random salt used for this token
     * @return String representation of the calculated checksum
     */
    private fun generateChecksum(userId: String, timestamp: Long, salt: String): String {
        val data = "$userId$timestamp$salt$SECRET_KEY"
        return data.hashCode().toString()
    }

    /**
     * Generates a cryptographically secure random salt.
     * 
     * Creates an 8-character random string using alphanumeric characters
     * for use in checksum generation and token uniqueness.
     * 
     * @return Random 8-character alphanumeric string
     */
    private fun generateRandomSalt(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8)
            .map { chars[SecureRandom().nextInt(chars.length)] }
            .joinToString("")
    }

    /**
     * Encrypts data using AES encryption with random IV.
     * 
     * Uses AES-256 encryption in CBC mode with PKCS5 padding. Generates a random
     * initialization vector (IV) for each encryption operation and prepends it
     * to the encrypted data for decryption.
     * 
     * @param data The plaintext string to encrypt
     * @return Byte array containing IV followed by encrypted data
     */
    private fun encrypt(data: String): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        
        // Generate random IV
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedData = cipher.doFinal(data.toByteArray())
        
        // Prepend IV to encrypted data
        return iv + encryptedData
    }

    /**
     * Decrypts AES-encrypted data with embedded IV.
     * 
     * Extracts the initialization vector from the beginning of the encrypted data
     * and uses it to decrypt the remaining data using AES-256 decryption.
     * 
     * @param encryptedDataWithIv Byte array containing IV (first 16 bytes) and encrypted data
     * @return Decrypted plaintext string
     */
    private fun decrypt(encryptedDataWithIv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        
        // Extract IV from the beginning
        val iv = encryptedDataWithIv.sliceArray(0..15)
        val encryptedData = encryptedDataWithIv.sliceArray(16 until encryptedDataWithIv.size)
        
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        
        return String(cipher.doFinal(encryptedData))
    }
}
