package com.example.proyecto_appmovil.ui.screens.admin

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.proyecto_appmovil.ui.theme.ItsurCiruela
import java.io.File

@Composable
fun PdfViewerDialog(file: File, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var pageIndex by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    var scale by remember { mutableFloatStateOf(1f) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val descriptor = remember(file) {
        try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (_: Exception) {
            null
        }
    }
    val renderer = remember(descriptor) {
        descriptor?.let {
            try {
                PdfRenderer(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    DisposableEffect(renderer) {
        onDispose {
            renderer?.close()
            descriptor?.close()
        }
    }

    LaunchedEffect(renderer, pageIndex) {
        if (renderer == null) {
            error = "No se pudo abrir el PDF."
            return@LaunchedEffect
        }
        try {
            pageCount = renderer.pageCount
            pageIndex = pageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            val page = renderer.openPage(pageIndex)
            val rendered = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(rendered, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmap = rendered
            error = null
        } catch (exception: Exception) {
            error = exception.message ?: "No se pudo renderizar la página."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cerrar") }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Abrir PDF"))
            }) {
                Text("Abrir en app externa")
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
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        error != null -> Text(error ?: "Error")
                        bitmap == null -> CircularProgressIndicator()
                        else -> Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Página $pageIndex del PDF",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(scaleX = scale, scaleY = scale)
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
                    ) { Text("Anterior") }
                    OutlinedButton(
                        onClick = { pageIndex = (pageIndex + 1).coerceAtMost(pageCount - 1) },
                        enabled = pageIndex < pageCount - 1,
                        modifier = Modifier.weight(1f)
                    ) { Text("Siguiente") }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { scale = (scale - 0.25f).coerceIn(1f, 5f) }, modifier = Modifier.weight(1f)) { Text("-") }
                    Button(
                        onClick = { scale = 1f },
                        colors = ButtonDefaults.buttonColors(containerColor = ItsurCiruela),
                        modifier = Modifier.weight(1f)
                    ) { Text("Restablecer") }
                    OutlinedButton(onClick = { scale = (scale + 0.25f).coerceIn(1f, 5f) }, modifier = Modifier.weight(1f)) { Text("+") }
                }
            }
        }
    )
}
