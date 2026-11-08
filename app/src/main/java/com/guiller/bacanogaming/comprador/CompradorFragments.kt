package com.guiller.bacanogaming.comprador

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.BaseNavActivity
import com.guiller.bacanogaming.common.ListaAdapter
import com.guiller.bacanogaming.common.Pedido
import com.guiller.bacanogaming.common.Producto
import com.guiller.bacanogaming.common.Sesion
import com.guiller.bacanogaming.common.Estado
import com.guiller.bacanogaming.common.aplicarEstado

class HomeFragment : Fragment(R.layout.fragment_home)

class CatalogFragment : Fragment(R.layout.fragment_catalog) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productos = listOf(
            Producto("Monitor Gamer ASUS ROG", "\$3.500.000"),
            Producto("Tarjeta gráfica RTX", "\$3.900.000"),
            Producto("Teclado mecánico RGB", "\$380.000"),
            Producto("Auriculares inalámbricos", "\$450.000"),
            Producto("PlayStation 5 Standard", "\$3.199.000"),
            Producto("Silla gamer ergonómica", "\$1.200.000")
        )
        view.findViewById<RecyclerView>(R.id.rvProductos).adapter = ListaAdapter(
            productos,
            R.layout.item_catalog_product,
            bind = { v, p ->
                v.findViewById<TextView>(R.id.tvNombreProducto).text = p.nombre
                v.findViewById<TextView>(R.id.tvPrecioProducto).text = p.precio
            },
            onClick = {
                (requireActivity() as BaseNavActivity).abrirPantalla(ProductDetailFragment())
            }
        )
    }
}

class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.ivVolver).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<View>(R.id.btnAgregarCarrito).setOnClickListener {
            Toast.makeText(requireContext(), R.string.msg_added_cart, Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btnListaDeseos).setOnClickListener {
            Toast.makeText(requireContext(), R.string.msg_wishlist_added, Toast.LENGTH_SHORT).show()
        }
    }
}

class OrdersFragment : Fragment(R.layout.fragment_orders) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pedidos = listOf(
            Pedido("#1028", "17 nov 2026", "2", "\$1.899.900", Estado.PAGADO),
            Pedido("#1025", "12 nov 2026", "1", "\$3.199.000", Estado.ENVIADO),
            Pedido("#1020", "03 nov 2026", "3", "\$950.000", Estado.ENTREGADO)
        )
        view.findViewById<RecyclerView>(R.id.rvPedidos).adapter = ListaAdapter(
            pedidos,
            R.layout.item_order,
            bind = { v, p ->
                v.findViewById<TextView>(R.id.tvPedidoId).text = p.id
                aplicarEstado(v.findViewById(R.id.tvEstado), p.estado)
            }
        )
    }
}

class CartFragment : Fragment(R.layout.fragment_cart) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.rvCarrito).adapter =
            ListaAdapter(listOf(1, 2), R.layout.item_cart)
        view.findViewById<View>(R.id.btnContinuarPago).setOnClickListener {
            (requireActivity() as BaseNavActivity).abrirPantalla(CheckoutFragment())
        }
    }
}

class CheckoutFragment : Fragment(R.layout.fragment_checkout) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.ivVolverPago).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<View>(R.id.btnPagar).setOnClickListener {
            Toast.makeText(requireContext(), R.string.msg_payment_done, Toast.LENGTH_SHORT).show()
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
                .selectedItemId = R.id.nav_orders
        }
    }
}

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnCerrarSesion).setOnClickListener {
            Sesion.cerrar(requireActivity())
        }
    }
}