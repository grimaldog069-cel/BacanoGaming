package com.guiller.bacanogaming.vendedor

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.BaseNavActivity
import com.guiller.bacanogaming.common.Sesion

class SellerDashboardFragment : Fragment(R.layout.fragment_seller_dashboard)
class SellerOrdersFragment : Fragment(R.layout.fragment_seller_orders)
class SellerOffersFragment : Fragment(R.layout.fragment_seller_offers)

class SellerProductsFragment : Fragment(R.layout.fragment_seller_products) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.fabNuevoProducto).setOnClickListener {
            (requireActivity() as BaseNavActivity).abrirPantalla(NewProductFragment())
        }
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