package com.guiller.bacanogaming.common

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.guiller.bacanogaming.R

/**
 * Pantalla de inicio de sesión. Permite entrar con correo y contraseña
 * (Firebase Authentication) o con la huella digital, y lleva al registro
 * y a la recuperación de contraseña.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var btnIngresar: Button

    /** Enlaza las vistas del layout y configura los botones. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)
        btnIngresar = findViewById(R.id.btnIngresar)

        btnIngresar.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val clave = etContrasena.text.toString()
            if (correo.isEmpty() || clave.isEmpty()) {
                mostrar(getString(R.string.error_empty_fields))
            } else {
                iniciarSesion(correo, clave)
            }
        }

        findViewById<Button>(R.id.btnBiometria).setOnClickListener { iniciarBiometria() }

        findViewById<TextView>(R.id.tvOlvido).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        findViewById<TextView>(R.id.tvRegistro).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Valida el correo y la contraseña con Firebase Authentication.
     * Si son correctos, consulta el rol en Firestore y abre la pantalla del rol.
     */
    private fun iniciarSesion(correo: String, clave: String) {
        btnIngresar.isEnabled = false
        auth.signInWithEmailAndPassword(correo, clave)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user?.uid
                if (uid == null) {
                    btnIngresar.isEnabled = true
                } else {
                    Sesion.entrar(this, uid) { mensaje ->
                        mostrar(mensaje)
                        btnIngresar.isEnabled = true
                    }
                }
            }
            .addOnFailureListener { error ->
                val mensaje = if (error is FirebaseNetworkException) {
                    getString(R.string.error_network)
                } else {
                    getString(R.string.error_invalid_credentials)
                }
                mostrar(mensaje)
                btnIngresar.isEnabled = true
            }
    }

    /**
     * Pide la huella digital y, si es válida, entra con la última cuenta
     * que inició sesión en este dispositivo (la sesión que guarda Firebase).
     */
    private fun iniciarBiometria() {
        val usuario = auth.currentUser
        if (usuario == null) {
            mostrar(getString(R.string.biometric_first_login))
            return
        }

        val autenticadores = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        val estado = BiometricManager.from(this).canAuthenticate(autenticadores)
        if (estado != BiometricManager.BIOMETRIC_SUCCESS) {
            mostrar(getString(R.string.biometric_unavailable))
            return
        }

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Sesion.entrar(this@LoginActivity, usuario.uid) { mensaje -> mostrar(mensaje) }
                }
            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_title))
            .setSubtitle(getString(R.string.biometric_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_cancel))
            .setAllowedAuthenticators(autenticadores)
            .build()

        prompt.authenticate(info)
    }

    /** Muestra un mensaje corto en pantalla. */
    private fun mostrar(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}