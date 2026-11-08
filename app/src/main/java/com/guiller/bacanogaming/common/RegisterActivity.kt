package com.guiller.bacanogaming.common

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.guiller.bacanogaming.R

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRegistro)
        val etClave = findViewById<EditText>(R.id.etContrasenaRegistro)
        val etConfirmar = findViewById<EditText>(R.id.etConfirmarContrasena)
        val cbTerminos = findViewById<CheckBox>(R.id.cbTerminos)

        findViewById<Button>(R.id.btnCrearCuenta).setOnClickListener {
            val camposVacios = etNombre.text.isBlank() || etCorreo.text.isBlank() ||
                    etClave.text.isEmpty() || etConfirmar.text.isEmpty()

            when {
                camposVacios ->
                    Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
                etClave.text.toString() != etConfirmar.text.toString() ->
                    Toast.makeText(this, R.string.error_password_mismatch, Toast.LENGTH_SHORT).show()
                !cbTerminos.isChecked ->
                    Toast.makeText(this, R.string.error_terms, Toast.LENGTH_SHORT).show()
                else -> {
                    Toast.makeText(this, R.string.msg_account_created, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        findViewById<TextView>(R.id.tvIrLogin).setOnClickListener { finish() }
    }
}