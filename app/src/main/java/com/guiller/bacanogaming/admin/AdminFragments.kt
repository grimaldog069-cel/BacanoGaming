package com.guiller.bacanogaming.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.BaseNavActivity
import com.guiller.bacanogaming.common.Sesion

class AdminUsersFragment : Fragment(R.layout.fragment_admin_users)
class AdminApprovalsFragment : Fragment(R.layout.fragment_admin_approvals)
class AdminModerationFragment : Fragment(R.layout.fragment_admin_moderation)
class AdminCatalogFragment : Fragment(R.layout.fragment_admin_catalog)
class AdminReportFragment : Fragment(R.layout.fragment_admin_report)

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnVerReporte).setOnClickListener {
            (requireActivity() as BaseNavActivity).abrirPantalla(AdminReportFragment())
        }
        view.findViewById<View>(R.id.btnCerrarSesionAdmin).setOnClickListener {
            Sesion.cerrar(requireActivity())
        }
    }
}