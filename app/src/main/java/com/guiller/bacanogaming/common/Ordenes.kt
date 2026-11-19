package com.guiller.bacanogaming.common

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Un producto dentro de una orden. */
data class LineaOrden(
    val productoId: String,
    val nombre: String,
    val precio: Long,
    val cantidad: Long,
    val vendedorId: String
)

/** Orden guardada en la colección "orders" de Firestore. */
data class Orden(
    val id: String,
    val compradorId: String,
    val compradorNombre: String,
    val items: List<LineaOrden>,
    val subtotal: Long,
    val envio: Long,
    val total: Long,
    val estado: String,
    val direccion: String,
    val metodoPago: String,
    val fecha: Date?
)

/** Convierte un documento de Firestore en una [Orden]. */
@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.aOrden(): Orden {
    val lista = (get("items") as? List<Map<String, Any?>>).orEmpty()
    val lineas = lista.map { m ->
        LineaOrden(
            productoId = m["productoId"] as? String ?: "",
            nombre = m["nombre"] as? String ?: "",
            precio = (m["precio"] as? Number)?.toLong() ?: 0L,
            cantidad = (m["cantidad"] as? Number)?.toLong() ?: 0L,
            vendedorId = m["vendedorId"] as? String ?: ""
        )
    }
    return Orden(
        id = id,
        compradorId = getString("compradorId").orEmpty(),
        compradorNombre = getString("compradorNombre").orEmpty(),
        items = lineas,
        subtotal = getLong("subtotal") ?: 0L,
        envio = getLong("envio") ?: 0L,
        total = getLong("total") ?: 0L,
        estado = getString("estado") ?: "pagado",
        direccion = getString("direccion").orEmpty(),
        metodoPago = getString("metodoPago").orEmpty(),
        fecha = getTimestamp("fecha")?.toDate()
    )
}

/** Convierte el estado guardado en Firestore al valor que usa la etiqueta de color. */
fun estadoANumero(estado: String): Int = when (estado) {
    "enviado" -> Estado.ENVIADO
    "entregado" -> Estado.ENTREGADO
    "pendiente" -> Estado.PENDIENTE
    else -> Estado.PAGADO
}

/** Da formato a una fecha, por ejemplo "18 nov 2026, 10:30". */
fun formatoFecha(fecha: Date?): String {
    if (fecha == null) return ""
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es-CO")).format(fecha)
}

/** Crea las órdenes en Firestore a partir del carrito. */
object OrdenRepository {

    /**
     * Guarda una orden con el contenido del carrito y descuenta el stock de cada
     * producto en una sola operación (todo se guarda o nada). Si hay coordenadas,
     * se guardan como un GeoPoint junto a la dirección de entrega.
     * Al terminar bien vacía el carrito y llama a [alExito]; si falla, llama a [alFallar].
     * Por ahora el pago se registra como "pagado"; la pasarela real reemplazará este paso.
     */
    fun crear(
        direccion: String,
        metodoPago: String,
        latitud: Double?,
        longitud: Double?,
        alExito: () -> Unit,
        alFallar: () -> Unit
    ) {
        val usuario = FirebaseAuth.getInstance().currentUser
        val items = Carrito.lista()
        if (usuario == null || items.isEmpty()) {
            alFallar()
            return
        }
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(usuario.uid).get()
            .addOnSuccessListener { perfil ->
                val nombre = perfil.getString("nombre") ?: usuario.email.orEmpty()
                val lineas = items.map {
                    hashMapOf<String, Any>(
                        "productoId" to it.articulo.id,
                        "nombre" to it.articulo.nombre,
                        "precio" to it.articulo.precio,
                        "cantidad" to it.cantidad,
                        "vendedorId" to it.articulo.vendedorId
                    )
                }
                val orden = hashMapOf<String, Any>(
                    "compradorId" to usuario.uid,
                    "compradorNombre" to nombre,
                    "compradorCorreo" to usuario.email.orEmpty(),
                    "items" to lineas,
                    "vendedorIds" to items.map { it.articulo.vendedorId }.distinct(),
                    "subtotal" to Carrito.subtotal(),
                    "envio" to Carrito.envio(),
                    "total" to Carrito.total(),
                    "estado" to "pagado",
                    "direccion" to direccion,
                    "metodoPago" to metodoPago,
                    "fecha" to FieldValue.serverTimestamp()
                )
                if (latitud != null && longitud != null) {
                    orden["ubicacion"] = GeoPoint(latitud, longitud)
                }

                val lote = db.batch()
                lote.set(db.collection("orders").document(), orden)
                for (item in items) {
                    lote.update(
                        db.collection("products").document(item.articulo.id),
                        "stock",
                        FieldValue.increment(-item.cantidad.toLong())
                    )
                }
                lote.commit()
                    .addOnSuccessListener {
                        Carrito.vaciar()
                        alExito()
                    }
                    .addOnFailureListener { alFallar() }
            }
            .addOnFailureListener { alFallar() }
    }
}