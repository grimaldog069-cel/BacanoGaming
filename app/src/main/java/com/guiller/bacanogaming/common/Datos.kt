package com.guiller.bacanogaming.common

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.guiller.bacanogaming.R

object Estado {
    const val PAGADO = 0
    const val ENVIADO = 1
    const val ENTREGADO = 2
    const val PENDIENTE = 3
}

data class Producto(val nombre: String, val precio: String)

data class Pedido(
    val id: String,
    val fecha: String,
    val items: String,
    val total: String,
    val estado: Int
)

data class Fila(val titulo: String, val detalle: String, val extra: String = "")

fun aplicarEstado(tv: TextView, estado: Int) {
    val (texto, fondo, color) = when (estado) {
        Estado.PAGADO -> Triple(R.string.order_status_paid, R.drawable.bg_badge_paid, R.color.status_paid)
        Estado.ENVIADO -> Triple(R.string.order_status_shipped, R.drawable.bg_badge_shipped, R.color.status_shipped)
        Estado.ENTREGADO -> Triple(R.string.order_status_delivered, R.drawable.bg_badge_delivered, R.color.accent_purple)
        else -> Triple(R.string.order_status_pending, R.drawable.bg_badge_pending, R.color.status_pending)
    }
    tv.setText(texto)
    tv.setBackgroundResource(fondo)
    tv.setTextColor(ContextCompat.getColor(tv.context, color))
}