package com.guiller.bacanogaming.comprador

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.BaseNavActivity
import com.guiller.bacanogaming.common.Sesion

class HomeFragment : Fragment(R.layout.fragment_home)
class CatalogFragment : Fragment(R.layout.fragment_catalog)
class ProductDetailFragment : Fragment(R.layout.fragment_product_detail)
class OrdersFragment : Fragment(R.layout.fragment_orders)

class CartFragment : Fragment(R.layout.fragment_cart) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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