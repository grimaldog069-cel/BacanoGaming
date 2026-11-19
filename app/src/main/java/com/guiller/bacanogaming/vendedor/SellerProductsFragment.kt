package com.guiller.bacanogaming.vendedor

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.Articulo
import com.guiller.bacanogaming.common.BaseNavActivity
import com.guiller.bacanogaming.common.ImagenRepository
import com.guiller.bacanogaming.common.ListaAdapter
import com.guiller.bacanogaming.common.aArticulo
import com.guiller.bacanogaming.common.formatoPrecio

/**
 * Lista los productos del vendedor que inició sesión (READ) y permite
 * editarlos (UPDATE), activarlos o desactivarlos y eliminarlos (DELETE).
 * Los datos se leen en tiempo real de la colección "products" de Firestore.
 */
class SellerProductsFragment : Fragment(R.layout.fragment_seller_products) {

    private val db = FirebaseFirestore.getInstance()
    private var escucha: ListenerRegistration? = null
    private lateinit var lista: RecyclerView

    /** Configura el botón de crear y la escucha de los productos del vendedor. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lista = view.findViewById(R.id.rvProductosVendedor)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        view.findViewById<View>(R.id.fabNuevoProducto).setOnClickListener {
            (requireActivity() as BaseNavActivity).abrirPantalla(NewProductFragment())
        }

        escucha = db.collection("products").whereEqualTo("vendedorId", uid)
            .addSnapshotListener { instantanea, error ->
                if (error != null || instantanea == null) return@addSnapshotListener
                val productos = instantanea.documents
                    .map { it.aArticulo() }
                    .sortedBy { it.nombre.lowercase() }
                mostrarLista(productos)
            }
    }

    /** Deja de escuchar Firestore cuando la pantalla se destruye. */
    override fun onDestroyView() {
        escucha?.remove()
        super.onDestroyView()
    }

    /** Dibuja la lista de productos y enlaza los botones de cada tarjeta. */
    private fun mostrarLista(productos: List<Articulo>) {
        lista.adapter = ListaAdapter(productos, R.layout.item_seller_product, bind = { v, a ->
            ImagenRepository.mostrarEn(v.findViewById<ImageView>(R.id.ivProductoVendedor), a.imagen)
            v.findViewById<TextView>(R.id.tvNombreProductoVendedor).text = a.nombre
            v.findViewById<TextView>(R.id.tvPrecioProductoVendedor).text = formatoPrecio(a.precio)
            v.findViewById<TextView>(R.id.tvStockProductoVendedor).text =
                getString(R.string.product_stock_format, a.stock.toInt())

            // Interruptor: activa o desactiva el producto en Firestore
            val interruptor = v.findViewById<CompoundButton>(R.id.swActivo)
            interruptor.setOnCheckedChangeListener(null)
            interruptor.isChecked = a.activo
            interruptor.setOnCheckedChangeListener { _, marcado ->
                db.collection("products").document(a.id).update("activo", marcado)
            }

            v.findViewById<View>(R.id.tvEditar).setOnClickListener {
                val pantalla = NewProductFragment()
                pantalla.arguments = bundleOf("id" to a.id)
                (requireActivity() as BaseNavActivity).abrirPantalla(pantalla)
            }
            v.findViewById<View>(R.id.tvEliminar).setOnClickListener { confirmarEliminar(a) }
        })
    }

    /** Pide confirmación antes de borrar el producto. */
    private fun confirmarEliminar(producto: Articulo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.product_delete_title)
            .setMessage(R.string.product_delete_message)
            .setNegativeButton(R.string.users_btn_cancel, null)
            .setPositiveButton(R.string.seller_delete) { _, _ -> eliminar(producto) }
            .show()
    }

    /** DELETE: borra el producto de Firestore. */
    private fun eliminar(producto: Articulo) {
        db.collection("products").document(producto.id).delete()
            .addOnSuccessListener {
                context?.let { Toast.makeText(it, R.string.msg_product_deleted, Toast.LENGTH_SHORT).show() }
            }
            .addOnFailureListener {
                context?.let { Toast.makeText(it, R.string.msg_product_error, Toast.LENGTH_SHORT).show() }
            }
    }
}

