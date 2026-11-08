package com.guiller.bacanogaming.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ListaAdapter<T>(
    private val items: List<T>,
    private val layoutRes: Int,
    private val bind: (View, T) -> Unit = { _, _ -> },
    private val onClick: ((T) -> Unit)? = null
) : RecyclerView.Adapter<ListaAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vista = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return VH(vista)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        bind(holder.itemView, item)
        if (onClick != null) {
            holder.itemView.setOnClickListener { onClick.invoke(item) }
        }
    }
}