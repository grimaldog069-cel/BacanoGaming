package com.guiller.bacanogaming.admin

import androidx.fragment.app.Fragment
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.BaseNavActivity

/**
 * Pantalla principal del rol Administrador. Define su menú inferior
 * (panel, usuarios, aprobaciones, PQRS y ajustes) y el fragment de cada opción.
 */
class AdminActivity : BaseNavActivity() {

    override val menuRes = R.menu.menu_admin
    override val startItemId = R.id.nav_panel

    /** Devuelve la pantalla que corresponde a la opción tocada en el menú. */
    override fun fragmentFor(itemId: Int): Fragment? = when (itemId) {
        R.id.nav_panel -> AdminDashboardFragment()
        R.id.nav_users -> AdminUsersFragment()
        R.id.nav_approvals -> AdminApprovalsFragment()
        R.id.nav_pqrs -> AdminModerationFragment()
        R.id.nav_settings -> AdminCatalogFragment()
        else -> null
    }
}