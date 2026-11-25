package com.example.proyecto_appmovil.ui.screens.admin

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.proyecto_appmovil.ui.theme.ItsurCiruela
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun PdfViewerDialog(file: File, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var pageIndex by remember(file) { mutableIntStateOf(0) }
    var pageCount by remember(file) { mutableIntStateOf(0) }
    var scale by remember(file, pageIndex) { mutableFloatStateOf(1f) }
    var pan by remember(file, pageIndex) { mutableStateOf(Offset.Zero) }
    var bitmap by remember(file, pageIndex) { mutableStateOf<Bitmap?>(null) }
    var error by remember(file, pageIndex) { mutableStateOf<String?>(null) }
    var externalError by remember(file) { mutableStateOf<String?>(null) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val renderScale = ceil(scale * 2f) / 2f

    LaunchedEffect(file, pageIndex, renderScale, viewport) {
        if (viewport.width <= 0 || viewport.height <= 0) return@LaunchedEffect
        if (bitmap != null) delay(90)
        try {
            val (count, rendered) = withContext(Dispatchers.IO) {
                renderPdfPage(file, pageIndex, viewport, renderScale)
            }
            pageCount = count
            bitmap = rendered
            error = null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            if (bitmap == null) error = "No se pudo abrir o renderizar la página del PDF."
        }
    }

    fun transform(nextScale: Float, offset: Offset) {
        scale = nextScale
        val page = bitmap ?: return
        val fit = minOf(viewport.width.toFloat() / page.width, viewport.height.toFloat() / page.height)
        val maxX = ((page.width * fit * scale - viewport.width) / 2f).coerceAtLeast(0f)
        val maxY = ((page.height * fit * scale - viewport.height) / 2f).coerceAtLeast(0f)
        pan = Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cerrar", color = Color.Black) }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                try {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Abrir PDF"))
                    externalError = null
                } catch (_: ActivityNotFoundException) {
                    externalError = "No hay una aplicación disponible para abrir el PDF."
                }
            }) {
                Text("Abrir en app externa", color = Color.Black)
            }
        },
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Previsualización PDF")
                Text(if (pageCount > 0) "Página ${pageIndex + 1} de $pageCount" else "")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .onSizeChanged { viewport = it }
                        .clipToBounds()
                        .pointerInput(file, pageIndex) {
                            detectTransformGestures { centroid, movement, zoom, _ ->
                                val nextScale = (scale * zoom).coerceIn(1f, 5f)
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val pivot = centroid - center
                                transform(nextScale, (pan - pivot) * (nextScale / scale) + pivot + movement)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        error != null -> Text(error ?: "Error")
                        bitmap == null -> CircularProgressIndicator()
                        else -> Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Página ${pageIndex + 1} del PDF",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale, scaleY = scale,
                                    translationX = pan.x, translationY = pan.y
                                )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { pageIndex = (pageIndex - 1).coerceAtLeast(0) },
                        enabled = pageIndex > 0,
                        modifier = Modifier.weight(1f)
                    ) { Text("Anterior", color = Color.Black) }
                    OutlinedButton(
                        onClick = { pageIndex = (pageIndex + 1).coerceAtMost(pageCount - 1) },
                        enabled = pageIndex < pageCount - 1,
                        modifier = Modifier.weight(1f)
                    ) { Text("Siguiente", color = Color.Black) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { transform((scale - 0.25f).coerceAtLeast(1f), pan) },
                        modifier = Modifier.weight(1f)
                    ) { Text("-", color = Color.Black) }
                    Button(
                        onClick = { transform(1f, Offset.Zero) },
                        colors = ButtonDefaults.buttonColors(containerColor = ItsurCiruela),
                        modifier = Modifier.weight(1f)
                    ) { Text("Restablecer") }
                    OutlinedButton(
                        onClick = { transform((scale + 0.25f).coerceAtMost(5f), pan) },
                        modifier = Modifier.weight(1f)
                    ) { Text("+", color = Color.Black) }
                }
                externalError?.let { Text(it) }
            }
        }
    )
}

private fun renderPdfPage(file: File, index: Int, viewport: IntSize, zoom: Float): Pair<Int, Bitmap> {
    val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = try {
        PdfRenderer(descriptor)
    } catch (exception: Exception) {
        descriptor.close()
        throw exception
    }
    return renderer.use { pdf ->
        pdf.openPage(index).use { page ->
            val fit = minOf(viewport.width.toFloat() / page.width, viewport.height.toFloat() / page.height)
            val scale = minOf(fit * zoom, 2560f / maxOf(page.width, page.height))
            val bitmap = Bitmap.createBitmap(
                (page.width * scale).roundToInt().coerceAtLeast(1),
                (page.height * scale).roundToInt().coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdf.pageCount to bitmap
        }
    }
}
