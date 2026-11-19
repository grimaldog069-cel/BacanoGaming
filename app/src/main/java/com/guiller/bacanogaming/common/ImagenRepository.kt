package com.guiller.bacanogaming.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.guiller.bacanogaming.R
import java.io.ByteArrayOutputStream

/**
 * Maneja las imágenes de los productos. Como Firebase Storage exige un plan
 * de pago, la foto se reduce, se comprime como JPEG y se guarda como texto
 * (Base64) dentro del documento del producto en Firestore.
 * Si algún día se usa Storage, solo hay que cambiar esta clase.
 */
object ImagenRepository {

    private const val LADO_MAXIMO = 800
    private const val CALIDAD = 70

    /** Lee una imagen de la galería y la devuelve reducida y codificada, o null si falla. */
    fun uriABase64(context: Context, uri: Uri): String? {
        val bitmap = decodificar(context, uri) ?: return null
        return bitmapABase64(bitmap)
    }

    /** Reduce el bitmap, lo comprime como JPEG y lo convierte a texto Base64. */
    fun bitmapABase64(original: Bitmap): String {
        val reducido = reducir(original)
        val salida = ByteArrayOutputStream()
        reducido.compress(Bitmap.CompressFormat.JPEG, CALIDAD, salida)
        return Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
    }

    /** Convierte un texto Base64 en imagen. Devuelve null si está vacío o es inválido. */
    fun base64ABitmap(texto: String): Bitmap? {
        if (texto.isEmpty()) return null
        return try {
            val bytes = Base64.decode(texto, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /**
     * Muestra la imagen en el ImageView. Si no hay imagen, pone el ícono
     * genérico de producto con su color y relleno originales.
     */
    fun mostrarEn(vista: ImageView, base64: String) {
        val bitmap = base64ABitmap(base64)
        if (bitmap != null) {
            vista.setPadding(0, 0, 0, 0)
            vista.imageTintList = null
            vista.scaleType = ImageView.ScaleType.CENTER_CROP
            vista.setImageBitmap(bitmap)
        } else {
            val relleno = (18 * vista.resources.displayMetrics.density).toInt()
            vista.setPadding(relleno, relleno, relleno, relleno)
            vista.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(vista.context, R.color.primary_blue_dark)
            )
            vista.scaleType = ImageView.ScaleType.CENTER_INSIDE
            vista.setImageResource(R.drawable.ic_products)
        }
    }

    /** Decodifica la imagen de la galería respetando su orientación y reduciendo su tamaño. */
    private fun decodificar(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= 28) {
                val fuente = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(fuente) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val mayor = maxOf(info.size.width, info.size.height)
                    if (mayor > LADO_MAXIMO) {
                        val escala = LADO_MAXIMO.toFloat() / mayor
                        decoder.setTargetSize(
                            (info.size.width * escala).toInt(),
                            (info.size.height * escala).toInt()
                        )
                    }
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Reduce el bitmap para que su lado mayor no pase de [LADO_MAXIMO] píxeles. */
    private fun reducir(b: Bitmap): Bitmap {
        val mayor = maxOf(b.width, b.height)
        if (mayor <= LADO_MAXIMO) return b
        val escala = LADO_MAXIMO.toFloat() / mayor
        return Bitmap.createScaledBitmap(
            b, (b.width * escala).toInt(), (b.height * escala).toInt(), true
        )
    }
}