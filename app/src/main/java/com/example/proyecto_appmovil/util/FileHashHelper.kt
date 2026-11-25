package com.example.proyecto_appmovil.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.DataInputStream
import java.io.EOFException
import java.io.File
import java.io.IOException
import java.security.DigestOutputStream
import java.security.MessageDigest

data class InternalFileInfo(
    val file: File,
    val originalName: String,
    val sha256Hex: String
)

data class PdfMetadata(
    val originalName: String,
    val sizeBytes: Long
)

object FileHashHelper {
    private const val MAX_BYTES = 5L * 1024 * 1024
    const val PDF_MIME_TYPE = "application/pdf"

    fun inspectPdf(context: Context, uri: Uri): PdfMetadata {
        require(uri.scheme == "content") { "Selecciona un archivo PDF en el dispositivo." }
        val resolver = context.contentResolver
        val columns = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        var originalName = ""
        var reportedSize = -1L
        resolver.query(uri, columns, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                reportedSize = cursor.getLong(sizeIndex)
                require(reportedSize <= MAX_BYTES) { "El PDF no puede superar 5 MB." }
            }
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                originalName = cursor.getString(nameIndex).orEmpty()
            }
        }

        val mimeType = resolver.getType(uri)
        require(mimeType == null || mimeType == PDF_MIME_TYPE || mimeType == "application/octet-stream") {
            "Selecciona un archivo PDF."
        }

        val input = resolver.openInputStream(uri) ?: throw IOException("No se pudo abrir el PDF seleccionado.")
        input.buffered().use { source ->
            val header = ByteArray(5)
            val validHeader = try {
                DataInputStream(source).readFully(header)
                header.contentEquals("%PDF-".toByteArray(Charsets.US_ASCII))
            } catch (_: EOFException) {
                false
            }
            require(validHeader) { "El archivo seleccionado no tiene un encabezado PDF válido." }
        }

        return PdfMetadata(
            originalName = originalName.ifBlank { "documento.pdf" },
            sizeBytes = reportedSize.coerceAtLeast(0L)
        )
    }

    fun copyToInternalAndComputeSha256(context: Context, uri: Uri): InternalFileInfo {
        require(uri.scheme == "content") { "Selecciona un archivo PDF en el dispositivo." }
        val resolver = context.contentResolver
        val columns = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        val originalName = resolver.query(uri, columns, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null

            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                require(cursor.getLong(sizeIndex) <= MAX_BYTES) { "El PDF no puede superar 5 MB." }
            }
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex == -1) null else cursor.getString(nameIndex)
        }.orEmpty()

        val mimeType = resolver.getType(uri)
        require(mimeType == null || mimeType == PDF_MIME_TYPE || mimeType == "application/octet-stream") {
            "Selecciona un archivo PDF."
        }

        val dir = docsDirectory(context)
        if (!dir.isDirectory && !dir.mkdirs()) throw IOException("No se pudo preparar el almacenamiento del PDF.")

        val file = File.createTempFile("documento_", ".pdf", dir)
        var copied = false
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val input = resolver.openInputStream(uri) ?: throw IOException("No se pudo abrir el PDF seleccionado.")
            input.buffered().use { source ->
                val header = ByteArray(5)
                val validHeader = try {
                    DataInputStream(source).readFully(header)
                    header.contentEquals("%PDF-".toByteArray(Charsets.US_ASCII))
                } catch (_: EOFException) {
                    false
                }
                require(validHeader) { "El archivo seleccionado no tiene un encabezado PDF válido." }

                DigestOutputStream(file.outputStream().buffered(), digest).use { output ->
                    var size = header.size.toLong()
                    output.write(header)
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var count = source.read(buffer)
                    while (count != -1) {
                        size += count
                        require(size <= MAX_BYTES) { "El PDF no puede superar 5 MB." }
                        output.write(buffer, 0, count)
                        count = source.read(buffer)
                    }
                }
            }

            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            val name = originalName.ifBlank { "documento.pdf" }
            copied = true
            return InternalFileInfo(file, name, hash)
        } finally {
            if (!copied) file.delete()
        }
    }

    fun docsDirectory(context: Context): File = File(context.filesDir, "docs")
}
