package com.guiller.bacanogaming.admin

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.BaseNavActivity
import com.guiller.bacanogaming.common.Fila
import com.guiller.bacanogaming.common.ListaAdapter
import com.guiller.bacanogaming.common.Sesion

class AdminReportFragment : Fragment(R.layout.fragment_admin_report)

class AdminUsersFragment : Fragment(R.layout.fragment_admin_users) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val usuarios = listOf(
            Fila("Ana Gómez", "Vendedor · Activo"),
            Fila("Carlos Ruiz", "Comprador · Activo"),
            Fila("Luisa Díaz", "Vendedor · Pendiente"),
            Fila("Pedro Martínez", "Comprador · Activo")
        )
        view.findViewById<RecyclerView>(R.id.rvUsuarios).adapter = ListaAdapter(
            usuarios,
            R.layout.item_admin_user,
            bind = { v, f ->
                v.findViewById<TextView>(R.id.tvNombreUsuarioAdmin).text = f.titulo
                v.findViewById<TextView>(R.id.tvRolEstado).text = f.detalle
            }
        )
    }
}

class AdminApprovalsFragment : Fragment(R.layout.fragment_admin_approvals) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val solicitudes = listOf(
            Fila("Tienda Gamer SAS (Vendedor)", "Documentos verificados: Sí"),
            Fila("Levels Tech (Vendedor)", "Documentos verificados: Pendiente"),
            Fila("Nova Gaming (Vendedor)", "Documentos verificados: Sí")
        )
        view.findViewById<RecyclerView>(R.id.rvAprobaciones).adapter = ListaAdapter(
            solicitudes,
            R.layout.item_approval,
            bind = { v, f ->
                v.findViewById<TextView>(R.id.tvNombreSolicitud).text = f.titulo
                v.findViewById<TextView>(R.id.tvDetalleSolicitud).text = f.detalle
            }
        )
    }
}

class AdminModerationFragment : Fragment(R.layout.fragment_admin_moderation) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val reportes = listOf(
            Fila("Reseña reportada: Mal servicio", "Usuario: María F.", "Estado: Recibida"),
            Fila("PQRS 3451: Reembolso no procesado", "Usuario: Juan P.", "Estado: En proceso"),
            Fila("Reseña reportada: Producto defectuoso", "Usuario: Pedro M.", "Estado: Resuelta")
        )
        view.findViewById<RecyclerView>(R.id.rvModeracion).adapter = ListaAdapter(
            reportes,
            R.layout.item_moderation,
            bind = { v, f ->
                v.findViewById<TextView>(R.id.tvTituloReporte).text = f.titulo
                v.findViewById<TextView>(R.id.tvUsuarioReporte).text = f.detalle
                v.findViewById<TextView>(R.id.tvEstadoReporte).text = f.extra
            }
        )
    }
}

class AdminCatalogFragment : Fragment(R.layout.fragment_admin_catalog) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val categorias = listOf(
            Fila("Periféricos", "Activo"),
            Fila("Componentes", "Activo"),
            Fila("Consolas", "Activo"),
            Fila("Videojuegos", "Activo")
        )
        view.findViewById<RecyclerView>(R.id.rvCatalogoAdmin).adapter = ListaAdapter(
            categorias,
            R.layout.item_catalog_admin,
            bind = { v, f ->
                v.findViewById<TextView>(R.id.tvNombreCatalogo).text = f.titulo
                v.findViewById<TextView>(R.id.tvEstadoCatalogo).text = f.detalle
            }
        )
    }
}

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