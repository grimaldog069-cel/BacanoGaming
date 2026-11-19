package com.guiller.bacanogaming.admin

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.ListaAdapter

/** Datos de un usuario guardado en la colección "users" de Firestore. */
data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val estado: String
)

/**
 * CRUD de usuarios para el rol Administrador.
 * Lee la colección "users" en tiempo real y permite crear, editar,
 * suspender o activar, y eliminar usuarios. Los datos se guardan en Firestore.
 */
class AdminUsersFragment : Fragment(R.layout.fragment_admin_users) {

    private val db = FirebaseFirestore.getInstance()
    private var todos: List<Usuario> = emptyList()
    private var textoBusqueda = ""
    private var escucha: ListenerRegistration? = null
    private lateinit var lista: RecyclerView

    /** Configura la búsqueda, el botón de crear y la escucha en tiempo real de Firestore. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lista = view.findViewById(R.id.rvUsuarios)

        view.findViewById<EditText>(R.id.etBuscarUsuario).addTextChangedListener { texto ->
            textoBusqueda = texto?.toString().orEmpty().trim()
            mostrarLista()
        }

        view.findViewById<View>(R.id.fabNuevoUsuario).setOnClickListener { mostrarDialogo(null) }

        // READ: cada cambio en la colección "users" actualiza la lista automáticamente
        escucha = db.collection("users").addSnapshotListener { instantanea, error ->
            if (error != null || instantanea == null) return@addSnapshotListener
            todos = instantanea.documents.map { doc ->
                Usuario(
                    id = doc.id,
                    nombre = doc.getString("nombre").orEmpty(),
                    correo = doc.getString("correo").orEmpty(),
                    rol = doc.getString("rol") ?: "comprador",
                    estado = doc.getString("estado") ?: "activo"
                )
            }.sortedBy { it.nombre.lowercase() }
            mostrarLista()
        }
    }

    /** Deja de escuchar Firestore cuando la pantalla se destruye. */
    override fun onDestroyView() {
        escucha?.remove()
        super.onDestroyView()
    }

    /** Aplica el texto de búsqueda y dibuja la lista de usuarios. */
    private fun mostrarLista() {
        val filtrados = todos.filter {
            it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    it.correo.contains(textoBusqueda, ignoreCase = true)
        }
        lista.adapter = ListaAdapter(filtrados, R.layout.item_admin_user, bind = { v, u ->
            val rolTexto = when (u.rol) {
                "admin" -> getString(R.string.users_role_admin)
                "vendedor" -> getString(R.string.register_role_seller)
                else -> getString(R.string.register_role_buyer)
            }
            val suspendido = u.estado == "suspendido"
            val estadoTexto = getString(
                if (suspendido) R.string.users_state_suspended else R.string.users_state_active
            )
            v.findViewById<TextView>(R.id.tvNombreUsuarioAdmin).text = u.nombre
            v.findViewById<TextView>(R.id.tvRolEstado).text = "$rolTexto · $estadoTexto"

            v.findViewById<Button>(R.id.btnEditarUsuario).setOnClickListener { mostrarDialogo(u) }
            v.findViewById<Button>(R.id.btnSuspenderUsuario).apply {
                setText(if (suspendido) R.string.users_activate else R.string.admin_user_suspend)
                setOnClickListener { cambiarEstado(u) }
            }
            v.findViewById<Button>(R.id.btnEliminarUsuario).setOnClickListener { confirmarEliminar(u) }
        })
    }

    /**
     * Muestra el formulario para crear un usuario (si [usuario] es null)
     * o para editar uno existente.
     */
    private fun mostrarDialogo(usuario: Usuario?) {
        val vista = layoutInflater.inflate(R.layout.dialog_user, null)
        val etNombre = vista.findViewById<EditText>(R.id.dlgNombre)
        val etCorreo = vista.findViewById<EditText>(R.id.dlgCorreo)
        val etClave = vista.findViewById<EditText>(R.id.dlgClave)
        val grupoRol = vista.findViewById<RadioGroup>(R.id.dlgRol)

        if (usuario != null) {
            // Al editar no se cambian el correo ni la contraseña
            etNombre.setText(usuario.nombre)
            etCorreo.setText(usuario.correo)
            etCorreo.isEnabled = false
            etClave.visibility = View.GONE
            grupoRol.check(
                when (usuario.rol) {
                    "admin" -> R.id.rbDlgAdmin
                    "vendedor" -> R.id.rbDlgVendedor
                    else -> R.id.rbDlgComprador
                }
            )
        } else {
            grupoRol.check(R.id.rbDlgComprador)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (usuario != null) R.string.users_dialog_edit_title else R.string.users_dialog_new_title)
            .setView(vista)
            .setNegativeButton(R.string.users_btn_cancel, null)
            .setPositiveButton(R.string.users_btn_save) { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val correo = etCorreo.text.toString().trim()
                val clave = etClave.text.toString()
                val rol = when (grupoRol.checkedRadioButtonId) {
                    R.id.rbDlgAdmin -> "admin"
                    R.id.rbDlgVendedor -> "vendedor"
                    else -> "comprador"
                }
                if (usuario != null) {
                    actualizarUsuario(usuario, nombre, rol)
                } else {
                    crearUsuario(nombre, correo, clave, rol)
                }
            }
            .show()
    }

    /**
     * CREATE: crea la cuenta en Firebase Authentication y su perfil en Firestore.
     * Se usa una segunda instancia de Firebase para que el administrador
     * no pierda su propia sesión al crear la cuenta nueva.
     */
    private fun crearUsuario(nombre: String, correo: String, clave: String, rol: String) {
        when {
            nombre.isEmpty() || correo.isEmpty() || clave.isEmpty() ->
                return mostrar(getString(R.string.error_empty_fields))
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ->
                return mostrar(getString(R.string.error_invalid_email))
            clave.length < 6 ->
                return mostrar(getString(R.string.error_weak_password))
        }

        val contexto = requireContext().applicationContext
        val opciones = FirebaseApp.getInstance().options
        val appSecundaria = try {
            FirebaseApp.getInstance("secundaria")
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(contexto, opciones, "secundaria")
        }
        val authSecundaria = FirebaseAuth.getInstance(appSecundaria)

        authSecundaria.createUserWithEmailAndPassword(correo, clave)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user?.uid ?: return@addOnSuccessListener
                val perfil = hashMapOf(
                    "nombre" to nombre,
                    "correo" to correo,
                    "rol" to rol,
                    "estado" to "activo",
                    "fechaRegistro" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).set(perfil)
                    .addOnSuccessListener {
                        authSecundaria.signOut()
                        mostrar(getString(R.string.users_msg_created))
                    }
                    .addOnFailureListener {
                        authSecundaria.signOut()
                        mostrar(getString(R.string.users_msg_error))
                    }
            }
            .addOnFailureListener { error ->
                mostrar(
                    if (error is FirebaseAuthUserCollisionException) {
                        getString(R.string.error_email_in_use)
                    } else {
                        getString(R.string.users_msg_error)
                    }
                )
            }
    }

    /** UPDATE: cambia el nombre y el rol del usuario en Firestore. */
    private fun actualizarUsuario(usuario: Usuario, nombre: String, rol: String) {
        if (nombre.isEmpty()) return mostrar(getString(R.string.error_empty_fields))
        db.collection("users").document(usuario.id)
            .update(mapOf("nombre" to nombre, "rol" to rol))
            .addOnSuccessListener { mostrar(getString(R.string.users_msg_updated)) }
            .addOnFailureListener { mostrar(getString(R.string.users_msg_error)) }
    }

    /** Suspende o activa la cuenta. Un usuario suspendido no puede iniciar sesión. */
    private fun cambiarEstado(usuario: Usuario) {
        if (usuario.id == FirebaseAuth.getInstance().currentUser?.uid) {
            return mostrar(getString(R.string.users_msg_self))
        }
        val suspender = usuario.estado != "suspendido"
        db.collection("users").document(usuario.id)
            .update("estado", if (suspender) "suspendido" else "activo")
            .addOnSuccessListener {
                mostrar(
                    getString(if (suspender) R.string.users_msg_suspended else R.string.users_msg_activated)
                )
            }
            .addOnFailureListener { mostrar(getString(R.string.users_msg_error)) }
    }

    /** Pide confirmación antes de eliminar el perfil del usuario. */
    private fun confirmarEliminar(usuario: Usuario) {
        if (usuario.id == FirebaseAuth.getInstance().currentUser?.uid) {
            return mostrar(getString(R.string.users_msg_self))
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.users_delete_title)
            .setMessage(R.string.users_delete_message)
            .setNegativeButton(R.string.users_btn_cancel, null)
            .setPositiveButton(R.string.admin_user_delete) { _, _ -> eliminarUsuario(usuario) }
            .show()
    }

    /** DELETE: borra el perfil del usuario en Firestore. */
    private fun eliminarUsuario(usuario: Usuario) {
        db.collection("users").document(usuario.id).delete()
            .addOnSuccessListener { mostrar(getString(R.string.users_msg_deleted)) }
            .addOnFailureListener { mostrar(getString(R.string.users_msg_error)) }
    }

    /** Muestra un mensaje corto en pantalla. */
    private fun mostrar(mensaje: String) {
        context?.let { Toast.makeText(it, mensaje, Toast.LENGTH_LONG).show() }
    }
}