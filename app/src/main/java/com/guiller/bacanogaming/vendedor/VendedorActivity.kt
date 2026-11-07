package com.guiller.bacanogaming.vendedor

import androidx.fragment.app.Fragment
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.BaseNavActivity

class VendedorActivity : BaseNavActivity() {

    override val menuRes = R.menu.menu_vendedor
    override val startItemId = R.id.nav_seller_panel

    override fun fragmentFor(itemId: Int): Fragment? = when (itemId) {
        R.id.nav_seller_panel -> SellerDashboardFragment()
        R.id.nav_seller_products -> SellerProductsFragment()
        R.id.nav_seller_orders -> SellerOrdersFragment()
        R.id.nav_seller_reports -> SellerOffersFragment()
        R.id.nav_seller_profile -> SellerProfileFragment()
        else -> null
    }
}