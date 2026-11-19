package com.guiller.bacanogaming.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptador genérico para cualquier RecyclerView de la app.
 * Recibe la lista de datos, el layout de cada fila y una función [bind] que
 * llena la fila con los datos; opcionalmente recibe [onClick] para detectar toques.
 */
class ListaAdapter<T>(
    private val items: List<T>,
    private val layoutRes: Int,
    private val bind: (View, T) -> Unit = { _, _ -> },
    private val onClick: ((T) -> Unit)? = null
) : RecyclerView.Adapter<ListaAdapter.VH>() {

    /** Guarda la vista de una fila para poder reutilizarla al desplazar la lista. */
    class VH(view: View) : RecyclerView.ViewHolder(view)

    /** Crea la vista de una fila a partir del layout recibido. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vista = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return VH(vista)
    }

    /** Cantidad de filas de la lista. */
    override fun getItemCount() = items.size

    /** Llena la fila de la posición indicada con sus datos y activa el toque si existe. */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        bind(holder.itemView, item)
        if (onClick != null) {
            holder.itemView.setOnClickListener { onClick.invoke(item) }
        }
    }
}