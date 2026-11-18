package com.guiller.bacanogaming.common

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.guiller.bacanogaming.R

/**
 * Pantalla para recuperar la contraseña. Firebase envía al correo
 * un enlace para restablecerla.
 */
class ForgotPasswordActivity : AppCompatActivity() {

    /** Enlaza las vistas y configura los botones de la pantalla. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etCorreo = findViewById<EditText>(R.id.etCorreoRecuperar)

        findViewById<Button>(R.id.btnEnviarEnlace).setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            if (correo.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
            } else {
                enviarEnlace(correo)
            }
        }

        findViewById<TextView>(R.id.tvVolverLogin).setOnClickListener { finish() }
    }

    /** Pide a Firebase que envíe el correo de restablecimiento a la dirección dada. */
    private fun enviarEnlace(correo: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(correo)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.msg_reset_if_registered, Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_network, Toast.LENGTH_LONG).show()
            }
    }
}