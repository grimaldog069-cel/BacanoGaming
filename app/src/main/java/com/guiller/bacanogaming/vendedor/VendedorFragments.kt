package com.guiller.bacanogaming.vendedor

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.ListaAdapter
import com.guiller.bacanogaming.common.Sesion
import com.guiller.bacanogaming.common.aOrden
import com.guiller.bacanogaming.common.aplicarEstado
import com.guiller.bacanogaming.common.estadoANumero
import com.guiller.bacanogaming.common.formatoPrecio

/** Panel principal del vendedor (diseño). */
class SellerDashboardFragment : Fragment(R.layout.fragment_seller_dashboard)

/** Ofertas y reportes del vendedor (diseño). */
class SellerOffersFragment : Fragment(R.layout.fragment_seller_offers)

/**
 * Pedidos recibidos: lista en tiempo real las órdenes que incluyen productos
 * del vendedor y le permite marcarlas como enviadas.
 */
class SellerOrdersFragment : Fragment(R.layout.fragment_seller_orders) {

    private var escucha: ListenerRegistration? = null

    /** Escucha las órdenes del vendedor y las dibuja, las más recientes primero. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val lista = view.findViewById<RecyclerView>(R.id.rvPedidosVendedor)

        escucha = db.collection("orders").whereArrayContains("vendedorIds", uid)
            .addSnapshotListener { instantanea, error ->
                if (error != null || instantanea == null) return@addSnapshotListener
                val ordenes = instantanea.documents
                    .map { it.aOrden() }
                    .sortedByDescending { it.fecha?.time ?: Long.MAX_VALUE }
                lista.adapter = ListaAdapter(ordenes, R.layout.item_seller_order, bind = { v, o ->
                    // Solo se muestran los productos de este vendedor
                    val propios = o.items.filter { it.vendedorId == uid }
                    v.findViewById<TextView>(R.id.tvPedidoVendedorId).text = "#" + o.id.take(6).uppercase()
                    aplicarEstado(v.findViewById(R.id.tvEstadoPedido), estadoANumero(o.estado))
                    v.findViewById<TextView>(R.id.tvCompradorPedido).text =
                        getString(R.string.order_buyer_format, o.compradorNombre)
                    v.findViewById<TextView>(R.id.tvItemsPedidoVendedor).text =
                        propios.joinToString("\n") { "${it.cantidad} x ${it.nombre}" }
                    v.findViewById<TextView>(R.id.tvTotalPedidoVendedor).text = getString(
                        R.string.order_total_format,
                        formatoPrecio(propios.sumOf { it.precio * it.cantidad })
                    )

                    // El botón solo aparece mientras el pedido está pagado y sin enviar
                    val boton = v.findViewById<View>(R.id.btnMarcarEnviado)
                    boton.visibility = if (o.estado == "pagado") View.VISIBLE else View.GONE
                    boton.setOnClickListener {
                        db.collection("orders").document(o.id).update("estado", "enviado")
                            .addOnSuccessListener {
                                context?.let {
                                    Toast.makeText(it, R.string.msg_marked_shipped, Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                })
            }
    }

    /** Deja de escuchar Firestore cuando la pantalla se destruye. */
    override fun onDestroyView() {
        escucha?.remove()
        super.onDestroyView()
    }
}

/** Perfil del vendedor con el botón de cerrar sesión. */
class SellerProfileFragment : Fragment(R.layout.fragment_seller_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnCerrarSesionVendedor).setOnClickListener {
            Sesion.cerrar(requireActivity())
        }
    }
}