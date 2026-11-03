package com.example.proyecto_appmovil.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

data class InternalFileInfo(
    val file: File,
    val originalName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val sha256Hex: String
)

object FileHashHelper {

    fun copyToInternalAndComputeSha256(
        context: Context,
        uriString: String?,
        tipoCodigo: String,
        fallbackFileName: String = "${tipoCodigo.lowercase()}.pdf",
        defaultMimeType: String = "application/pdf"
    ): InternalFileInfo {
        val dir = File(context.filesDir, "documentos").apply { mkdirs() }
        val sanitizedPrefix = tipoCodigo.lowercase().replace(Regex("[^a-z0-9_]"), "_")
        val ext = if (defaultMimeType.startsWith("image/")) "jpg" else "pdf"
        val destFile = File(dir, "${sanitizedPrefix}_${System.currentTimeMillis()}.$ext")

        var resolvedName = fallbackFileName
        var resolvedMime = defaultMimeType

        if (!uriString.isNullOrBlank()) {
            runCatching {
                val uri = Uri.parse(uriString)
                if (uri.scheme == "content" || uri.scheme == "file") {
                    context.contentResolver.getType(uri)?.let { resolvedMime = it }
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst() && nameIndex >= 0) {
                            resolvedName = cursor.getString(nameIndex) ?: fallbackFileName
                        }
                    }
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                } else {
                    val existingFile = File(uriString)
                    if (existingFile.exists() && existingFile.isFile) {
                        resolvedName = existingFile.name
                        existingFile.copyTo(destFile, overwrite = true)
                    }
                }
            }
        }

        if (!destFile.exists() || destFile.length() == 0L) {
            val dummyPdfBytes = buildMinimalValidPdf(tipoCodigo, resolvedName)
            FileOutputStream(destFile).use { it.write(dummyPdfBytes) }
        }

        val sha256 = computeSha256(destFile)
        return InternalFileInfo(
            file = destFile,
            originalName = resolvedName,
            mimeType = resolvedMime,
            sizeBytes = destFile.length(),
            sha256Hex = sha256
        )
    }

    fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun computeSha256Bytes(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    private fun buildMinimalValidPdf(tipoCodigo: String, fileName: String): ByteArray {
        val content = """
            %PDF-1.4
            1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
            2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj
            3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >> endobj
            % ITSUR Documento: $tipoCodigo - $fileName - Timestamp: ${System.currentTimeMillis()}
            xref
            0 4
            0000000000 65535 f 
            trailer << /Size 4 /Root 1 0 R >>
            startxref
            0
            %%EOF
        """.trimIndent()
        return content.toByteArray(Charsets.UTF_8)
    }
}
