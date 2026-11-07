package com.guiller.bacanogaming.comprador

import androidx.fragment.app.Fragment
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.BaseNavActivity

class CompradorActivity : BaseNavActivity() {

    override val menuRes = R.menu.menu_comprador
    override val startItemId = R.id.nav_home

    override fun fragmentFor(itemId: Int): Fragment? = when (itemId) {
        R.id.nav_home -> HomeFragment()
        R.id.nav_search -> CatalogFragment()
        R.id.nav_cart -> CartFragment()
        R.id.nav_orders -> OrdersFragment()
        R.id.nav_profile -> ProfileFragment()
        else -> null
    }
}