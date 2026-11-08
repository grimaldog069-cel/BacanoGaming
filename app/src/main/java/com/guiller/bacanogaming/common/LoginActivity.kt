package com.guiller.bacanogaming.common

import android.content.Context
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
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.admin.AdminActivity
import com.guiller.bacanogaming.comprador.CompradorActivity
import com.guiller.bacanogaming.vendedor.VendedorActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)

        findViewById<Button>(R.id.btnIngresar).setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val clave = etContrasena.text.toString()

            if (correo.isEmpty() || clave.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rol = when {
                correo.contains("admin", ignoreCase = true) -> "admin"
                correo.contains("vendedor", ignoreCase = true) -> "vendedor"
                else -> "comprador"
            }
            getSharedPreferences("sesion", Context.MODE_PRIVATE)
                .edit().putString("rol", rol).apply()
            abrirSegunRol(rol)
        }

        findViewById<Button>(R.id.btnBiometria).setOnClickListener { iniciarBiometria() }

        findViewById<TextView>(R.id.tvOlvido).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        findViewById<TextView>(R.id.tvRegistro).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun abrirSegunRol(rol: String) {
        val destino = when (rol) {
            "admin" -> AdminActivity::class.java
            "vendedor" -> VendedorActivity::class.java
            else -> CompradorActivity::class.java
        }
        startActivity(Intent(this, destino))
        finish()
    }

    private fun iniciarBiometria() {
        val rol = getSharedPreferences("sesion", Context.MODE_PRIVATE).getString("rol", null)
        if (rol == null) {
            Toast.makeText(this, R.string.biometric_first_login, Toast.LENGTH_LONG).show()
            return
        }

        val autenticadores = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        val estado = BiometricManager.from(this).canAuthenticate(autenticadores)
        if (estado != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, R.string.biometric_unavailable, Toast.LENGTH_LONG).show()
            return
        }

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    abrirSegunRol(rol)
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
}