package com.guiller.bacanogaming.common

import com.google.firebase.firestore.DocumentSnapshot
import java.text.NumberFormat
import java.util.Locale

/** Producto guardado en la colección "products" de Firestore. */
data class Articulo(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val precio: Long,
    val stock: Long,
    val activo: Boolean,
    val vendedorId: String,
    val imagen: String
)

/** Convierte un documento de Firestore en un [Articulo]. */
fun DocumentSnapshot.aArticulo(): Articulo = Articulo(
    id = id,
    nombre = getString("nombre").orEmpty(),
    descripcion = getString("descripcion").orEmpty(),
    categoria = getString("categoria").orEmpty(),
    precio = getLong("precio") ?: 0L,
    stock = getLong("stock") ?: 0L,
    activo = getBoolean("activo") ?: true,
    vendedorId = getString("vendedorId").orEmpty(),
    imagen = getString("imagen").orEmpty()
)

/** Da formato de pesos colombianos a un valor, por ejemplo "$ 3.500.000". */
fun formatoPrecio(valor: Long): String {
    val formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"))
    formato.maximumFractionDigits = 0
    return formato.format(valor)
}

