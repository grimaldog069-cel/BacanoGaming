package com.guiller.bacanogaming.vendedor

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.BaseNavActivity
import com.guiller.bacanogaming.common.Estado
import com.guiller.bacanogaming.common.ListaAdapter
import com.guiller.bacanogaming.common.Pedido
import com.guiller.bacanogaming.common.Sesion
import com.guiller.bacanogaming.common.aplicarEstado

class SellerDashboardFragment : Fragment(R.layout.fragment_seller_dashboard)
class SellerOffersFragment : Fragment(R.layout.fragment_seller_offers)

class SellerProductsFragment : Fragment(R.layout.fragment_seller_products) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.rvProductosVendedor).adapter =
            ListaAdapter(listOf(1, 2, 3, 4), R.layout.item_seller_product)
        view.findViewById<View>(R.id.fabNuevoProducto).setOnClickListener {
            (requireActivity() as BaseNavActivity).abrirPantalla(NewProductFragment())
        }
    }
}

class SellerOrdersFragment : Fragment(R.layout.fragment_seller_orders) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pedidos = listOf(
            Pedido("#1235", "", "", "", Estado.PENDIENTE),
            Pedido("#1233", "", "", "", Estado.ENVIADO),
            Pedido("#1230", "", "", "", Estado.PAGADO)
        )
        view.findViewById<RecyclerView>(R.id.rvPedidosVendedor).adapter = ListaAdapter(
            pedidos,
            R.layout.item_seller_order,
            bind = { v, p ->
                v.findViewById<TextView>(R.id.tvPedidoVendedorId).text = p.id
                aplicarEstado(v.findViewById(R.id.tvEstadoPedido), p.estado)
            }
        )
    }
}

class NewProductFragment : Fragment(R.layout.fragment_new_product) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.ivVolverNuevo).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<View>(R.id.btnPublicar).setOnClickListener {
            Toast.makeText(requireContext(), R.string.msg_product_published, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }
}

class SellerProfileFragment : Fragment(R.layout.fragment_seller_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnCerrarSesionVendedor).setOnClickListener {
            Sesion.cerrar(requireActivity())
        }
    }
}