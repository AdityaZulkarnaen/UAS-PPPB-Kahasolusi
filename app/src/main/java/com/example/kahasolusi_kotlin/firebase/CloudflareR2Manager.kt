package com.example.kahasolusi_kotlin.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.kahasolusi_kotlin.config.R2Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * CloudflareR2Manager - Manages image storage using Cloudflare R2
 * Using OkHttp with AWS Signature V4 for compatibility
 */
class CloudflareR2Manager(private val context: Context) {
    
    companion object {
        private const val TAG = "CloudflareR2Manager"
        private const val AWS_ALGORITHM = "AWS4-HMAC-SHA256"
        private const val AWS_REQUEST_TYPE = "aws4_request"
    }
    
    private val httpClient = OkHttpClient()
    
    /**
     * Upload gambar ke R2 dari URI
     * @param imageUri URI gambar lokal yang akan diupload
     * @param folder Folder tujuan di R2 (portfolio/technology)
     * @return Result dengan public URL jika berhasil
     */
    suspend fun uploadImage(imageUri: Uri, folder: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!R2Config.isConfigured()) {
                return@withContext Result.failure(Exception("R2 credentials belum dikonfigurasi. Cek R2Config.kt"))
            }
            
            // Generate unique filename
            val extension = getFileExtension(context, imageUri)
            val fileName = "${UUID.randomUUID()}.$extension"
            val s3Key = "$folder/$fileName"
            
            // Get input stream dan baca bytes
            val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Result.failure(Exception("Tidak dapat membuka file"))
            
            val imageBytes = inputStream.readBytes()
            inputStream.close()
            
            // Get content type
            val contentType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            
            // Upload ke R2 menggunakan signed request
            val uploadResult = uploadToR2(s3Key, imageBytes, contentType)
            
            if (uploadResult) {
                val publicUrl = R2Config.getPublicUrl(folder, fileName)
                Log.d(TAG, "Upload berhasil: $publicUrl")
                Result.success(publicUrl)
            } else {
                Result.failure(Exception("Upload gagal"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Gagal upload gambar: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Upload file ke R2 menggunakan HTTP PUT dengan AWS Signature V4
     */
    private fun uploadToR2(key: String, data: ByteArray, contentType: String): Boolean {
        try {
            val url = "${R2Config.ENDPOINT}/${R2Config.BUCKET_NAME}/$key"
            val dateTime = getAmzDate()
            val date = dateTime.substring(0, 8)
            
            // Calculate payload hash
            val payloadHash = sha256Hex(data)
            
            // Create canonical request
            val canonicalRequest = buildCanonicalRequest(
                method = "PUT",
                uri = "/${R2Config.BUCKET_NAME}/$key",
                queryString = "",
                headers = mapOf(
                    "content-type" to contentType,
                    "host" to R2Config.ENDPOINT.removePrefix("https://"),
                    "x-amz-content-sha256" to payloadHash,
                    "x-amz-date" to dateTime
                ),
                payloadHash = payloadHash
            )
            
            // Create string to sign
            val credentialScope = "$date/${R2Config.REGION}/s3/$AWS_REQUEST_TYPE"
            val stringToSign = buildStringToSign(dateTime, credentialScope, canonicalRequest)
            
            // Calculate signature
            val signature = calculateSignature(date, stringToSign)
            
            // Create authorization header
            val authorization = buildAuthorizationHeader(
                accessKey = R2Config.ACCESS_KEY_ID,
                credentialScope = credentialScope,
                signedHeaders = "content-type;host;x-amz-content-sha256;x-amz-date",
                signature = signature
            )
            
            // Make HTTP request
            val requestBody = data.toRequestBody(contentType.toMediaType())
            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .header("Authorization", authorization)
                .header("Content-Type", contentType)
                .header("x-amz-content-sha256", payloadHash)
                .header("x-amz-date", dateTime)
                .build()
            
            val response = httpClient.newCall(request).execute()
            val success = response.isSuccessful
            
            if (!success) {
                Log.e(TAG, "Upload failed: ${response.code} - ${response.body?.string()}")
            }
            
            response.close()
            return success
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading to R2: ${e.message}", e)
            return false
        }
    }
    
    private fun buildCanonicalRequest(
        method: String,
        uri: String,
        queryString: String,
        headers: Map<String, String>,
        payloadHash: String
    ): String {
        val sortedHeaders = headers.toSortedMap()
        val canonicalHeaders = sortedHeaders.map { "${it.key}:${it.value}\n" }.joinToString("")
        val signedHeaders = sortedHeaders.keys.joinToString(";")
        
        return "$method\n$uri\n$queryString\n$canonicalHeaders\n$signedHeaders\n$payloadHash"
    }
    
    private fun buildStringToSign(dateTime: String, credentialScope: String, canonicalRequest: String): String {
        val hashedCanonicalRequest = sha256Hex(canonicalRequest.toByteArray())
        return "$AWS_ALGORITHM\n$dateTime\n$credentialScope\n$hashedCanonicalRequest"
    }
    
    private fun calculateSignature(date: String, stringToSign: String): String {
        val kDate = hmacSHA256("AWS4${R2Config.SECRET_ACCESS_KEY}".toByteArray(), date)
        val kRegion = hmacSHA256(kDate, R2Config.REGION)
        val kService = hmacSHA256(kRegion, "s3")
        val kSigning = hmacSHA256(kService, AWS_REQUEST_TYPE)
        return hmacSHA256Hex(kSigning, stringToSign)
    }
    
    private fun buildAuthorizationHeader(
        accessKey: String,
        credentialScope: String,
        signedHeaders: String,
        signature: String
    ): String {
        return "$AWS_ALGORITHM Credential=$accessKey/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"
    }
    
    private fun getAmzDate(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(Date())
    }
    
    private fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02x".format(it) }
    }
    
    private fun hmacSHA256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray())
    }
    
    private fun hmacSHA256Hex(key: ByteArray, data: String): String {
        return hmacSHA256(key, data).joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Delete gambar dari R2
     * @param imageUrl Public URL gambar yang akan dihapus
     * @return Result success jika berhasil
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!R2Config.isConfigured()) {
                return@withContext Result.failure(Exception("R2 credentials belum dikonfigurasi"))
            }
            
            // Extract S3 key from public URL
            val s3Key = extractS3KeyFromUrl(imageUrl) 
                ?: return@withContext Result.failure(Exception("Invalid image URL"))
            
            val deleteResult = deleteFromR2(s3Key)
            
            if (deleteResult) {
                Log.d(TAG, "Delete berhasil: $s3Key")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete gagal"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Gagal delete gambar: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete file dari R2 menggunakan HTTP DELETE dengan AWS Signature V4
     */
    private fun deleteFromR2(key: String): Boolean {
        try {
            val url = "${R2Config.ENDPOINT}/${R2Config.BUCKET_NAME}/$key"
            val dateTime = getAmzDate()
            val date = dateTime.substring(0, 8)
            
            // Payload hash for DELETE is always empty string hash
            val payloadHash = sha256Hex(ByteArray(0))
            
            // Create canonical request
            val canonicalRequest = buildCanonicalRequest(
                method = "DELETE",
                uri = "/${R2Config.BUCKET_NAME}/$key",
                queryString = "",
                headers = mapOf(
                    "host" to R2Config.ENDPOINT.removePrefix("https://"),
                    "x-amz-content-sha256" to payloadHash,
                    "x-amz-date" to dateTime
                ),
                payloadHash = payloadHash
            )
            
            // Create string to sign
            val credentialScope = "$date/${R2Config.REGION}/s3/$AWS_REQUEST_TYPE"
            val stringToSign = buildStringToSign(dateTime, credentialScope, canonicalRequest)
            
            // Calculate signature
            val signature = calculateSignature(date, stringToSign)
            
            // Create authorization header
            val authorization = buildAuthorizationHeader(
                accessKey = R2Config.ACCESS_KEY_ID,
                credentialScope = credentialScope,
                signedHeaders = "host;x-amz-content-sha256;x-amz-date",
                signature = signature
            )
            
            // Make HTTP request
            val request = Request.Builder()
                .url(url)
                .delete()
                .header("Authorization", authorization)
                .header("x-amz-content-sha256", payloadHash)
                .header("x-amz-date", dateTime)
                .build()
            
            val response = httpClient.newCall(request).execute()
            val success = response.isSuccessful
            
            if (!success) {
                Log.e(TAG, "Delete failed: ${response.code} - ${response.body?.string()}")
            }
            
            response.close()
            return success
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting from R2: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Update gambar (hapus yang lama dan upload yang baru)
     * @param oldImageUrl URL gambar lama yang akan dihapus (optional)
     * @param newImageUri URI gambar baru yang akan diupload
     * @param folder Folder tujuan di R2
     * @return Result dengan public URL gambar baru jika berhasil
     */
    suspend fun updateImage(oldImageUrl: String?, newImageUri: Uri, folder: String): Result<String> {
        return try {
            // Upload gambar baru terlebih dahulu
            val uploadResult = uploadImage(newImageUri, folder)
            
            if (uploadResult.isSuccess && !oldImageUrl.isNullOrEmpty()) {
                // Jika upload berhasil, hapus gambar lama
                // Ignore error jika delete gagal
                try {
                    deleteImage(oldImageUrl)
                } catch (e: Exception) {
                    Log.w(TAG, "Gagal menghapus gambar lama (diabaikan): ${e.message}")
                }
            }
            
            uploadResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload portfolio image
     */
    suspend fun uploadPortfolioImage(imageUri: Uri): Result<String> {
        return uploadImage(imageUri, R2Config.PORTFOLIO_FOLDER)
    }
    
    /**
     * Upload technology icon
     */
    suspend fun uploadTechnologyIcon(imageUri: Uri): Result<String> {
        return uploadImage(imageUri, R2Config.TECHNOLOGY_FOLDER)
    }
    
    /**
     * Extract S3 key dari public URL
     * Mendukung berbagai format URL:
     * - https://pub-xxx.r2.dev/portfolio/uuid.jpg -> portfolio/uuid.jpg
     * - https://xxx.r2.cloudflarestorage.com/kahasolusi/portfolio/uuid.jpg -> portfolio/uuid.jpg
     * - https://custom-domain.com/portfolio/uuid.jpg -> portfolio/uuid.jpg
     */
    private fun extractS3KeyFromUrl(url: String): String? {
        return try {
            val uri = Uri.parse(url)
            val path = uri.path ?: return null
            
            // Remove leading slash
            val cleanPath = path.removePrefix("/")
            
            // Jika URL dari endpoint S3 API (ada bucket name di path)
            if (cleanPath.startsWith("${R2Config.BUCKET_NAME}/")) {
                return cleanPath.removePrefix("${R2Config.BUCKET_NAME}/")
            }
            
            // Jika URL dari R2.dev atau custom domain (tidak ada bucket name)
            // Path langsung berupa folder/filename
            return cleanPath
            
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting S3 key: ${e.message}")
            null
        }
    }
    
    /**
     * Get file extension dari URI
     */
    private fun getFileExtension(context: Context, uri: Uri): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg"
        }
    }
    
    /**
     * Get file size dari URI
     */
    private fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Check if R2 is properly configured
     */
    fun isConfigured(): Boolean {
        return R2Config.isConfigured()
    }
}
