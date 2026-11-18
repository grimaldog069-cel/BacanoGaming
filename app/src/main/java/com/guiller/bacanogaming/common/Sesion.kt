package com.guiller.bacanogaming.common

import android.app.Activity
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.admin.AdminActivity
import com.guiller.bacanogaming.comprador.CompradorActivity
import com.guiller.bacanogaming.vendedor.VendedorActivity

/**
 * Funciones comunes para manejar la sesión del usuario: entrar según el rol
 * guardado en Firestore y cerrar sesión.
 */
object Sesion {

    /**
     * Regresa a la pantalla de Login y borra el historial de pantallas.
     * No cierra la sesión de Firebase a propósito: así el botón de huella
     * puede volver a entrar con la última cuenta usada en este dispositivo.
     */
    fun cerrar(activity: Activity) {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
    }

    /**
     * Abre la pantalla principal que corresponde al rol recibido
     * ("admin", "vendedor" o cualquier otro valor, que se trata como comprador).
     */
    fun abrirSegunRol(activity: Activity, rol: String) {
        val destino = when (rol) {
            "admin" -> AdminActivity::class.java
            "vendedor" -> VendedorActivity::class.java
            else -> CompradorActivity::class.java
        }
        activity.startActivity(Intent(activity, destino))
        activity.finish()
    }

    /**
     * Lee el perfil del usuario en la colección "users" de Firestore usando su uid.
     * Si el perfil existe y no está suspendido, abre la pantalla de su rol.
     * En caso contrario llama a [alFallar] con el mensaje que se debe mostrar.
     */
    fun entrar(activity: Activity, uid: String, alFallar: (String) -> Unit) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    alFallar(activity.getString(R.string.error_no_profile))
                } else if (doc.getString("estado") == "suspendido") {
                    FirebaseAuth.getInstance().signOut()
                    alFallar(activity.getString(R.string.error_account_suspended))
                } else {
                    abrirSegunRol(activity, doc.getString("rol") ?: "comprador")
                }
            }
            .addOnFailureListener {
                alFallar(activity.getString(R.string.error_network))
            }
    }
}