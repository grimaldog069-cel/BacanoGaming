package com.guiller.bacanogaming.common

/** Producto dentro del carrito con la cantidad elegida. */
data class ItemCarrito(val articulo: Articulo, var cantidad: Int)

/**
 * Carrito de compras. Vive en memoria mientras la app está abierta;
 * al pagar, su contenido se guarda como una orden en Firestore.
 */
object Carrito {

    /** Costo fijo de envío por pedido. */
    const val ENVIO = 25000L

    private val items = mutableListOf<ItemCarrito>()

    /** Devuelve una copia de la lista de productos del carrito. */
    fun lista(): List<ItemCarrito> = items.toList()

    /** Indica si el carrito no tiene productos. */
    fun estaVacio(): Boolean = items.isEmpty()

    /** Agrega una unidad del producto. Devuelve false si ya no hay stock suficiente. */
    fun agregar(articulo: Articulo): Boolean {
        val existente = items.find { it.articulo.id == articulo.id }
        if (existente == null) {
            if (articulo.stock < 1) return false
            items.add(ItemCarrito(articulo, 1))
        } else {
            if (existente.cantidad + 1 > existente.articulo.stock) return false
            existente.cantidad++
        }
        return true
    }

    /**
     * Suma o resta unidades a un producto del carrito. Si la cantidad llega a cero
     * se quita del carrito. Devuelve false si se supera el stock disponible.
     */
    fun cambiarCantidad(item: ItemCarrito, cambio: Int): Boolean {
        val nueva = item.cantidad + cambio
        if (nueva > item.articulo.stock) return false
        if (nueva <= 0) items.remove(item) else item.cantidad = nueva
        return true
    }

    /** Quita el producto del carrito. */
    fun quitar(item: ItemCarrito) {
        items.remove(item)
    }

    /** Vacía el carrito (se usa después de registrar la orden). */
    fun vaciar() {
        items.clear()
    }

    /** Suma del precio de todos los productos por su cantidad. */
    fun subtotal(): Long = items.sumOf { it.articulo.precio * it.cantidad }

    /** Envío: fijo si hay productos, cero si el carrito está vacío. */
    fun envio(): Long = if (items.isEmpty()) 0L else ENVIO

    /** Subtotal más envío. */
    fun total(): Long = subtotal() + envio()
}