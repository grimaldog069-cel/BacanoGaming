package com.guiller.bacanogaming.common

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.guiller.bacanogaming.R

/**
 * Pantalla de registro. Crea la cuenta en Firebase Authentication y guarda
 * el perfil (nombre, correo, rol y estado) en la colección "users" de Firestore.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var btnCrear: Button

    /** Enlaza las vistas y configura el botón de crear cuenta. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etCorreo = findViewById<EditText>(R.id.etCorreoRegistro)
        val etClave = findViewById<EditText>(R.id.etContrasenaRegistro)
        val etConfirmar = findViewById<EditText>(R.id.etConfirmarContrasena)
        val rgRol = findViewById<RadioGroup>(R.id.rgRol)
        val cbTerminos = findViewById<CheckBox>(R.id.cbTerminos)
        btnCrear = findViewById(R.id.btnCrearCuenta)

        btnCrear.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val clave = etClave.text.toString()
            val confirmar = etConfirmar.text.toString()
            val rol = if (rgRol.checkedRadioButtonId == R.id.rbVendedor) "vendedor" else "comprador"

            when {
                nombre.isEmpty() || correo.isEmpty() || clave.isEmpty() || confirmar.isEmpty() ->
                    mostrar(getString(R.string.error_empty_fields))
                !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ->
                    mostrar(getString(R.string.error_invalid_email))
                clave.length < 6 ->
                    mostrar(getString(R.string.error_weak_password))
                clave != confirmar ->
                    mostrar(getString(R.string.error_password_mismatch))
                !cbTerminos.isChecked ->
                    mostrar(getString(R.string.error_terms))
                else -> crearCuenta(nombre, correo, clave, rol)
            }
        }

        findViewById<TextView>(R.id.tvIrLogin).setOnClickListener { finish() }
    }

    /**
     * Crea el usuario en Firebase Authentication y, si sale bien,
     * guarda su perfil en Firestore. Al terminar vuelve al Login.
     */
    private fun crearCuenta(nombre: String, correo: String, clave: String, rol: String) {
        btnCrear.isEnabled = false
        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(correo, clave)
            .addOnSuccessListener { resultado ->
                val uid = resultado.user?.uid
                if (uid == null) {
                    btnCrear.isEnabled = true
                    return@addOnSuccessListener
                }
                val perfil = hashMapOf(
                    "nombre" to nombre,
                    "correo" to correo,
                    "rol" to rol,
                    "estado" to "activo",
                    "terminosAceptados" to true,
                    "fechaRegistro" to FieldValue.serverTimestamp()
                )
                FirebaseFirestore.getInstance().collection("users").document(uid).set(perfil)
                    .addOnSuccessListener {
                        auth.signOut()
                        mostrar(getString(R.string.msg_account_created))
                        finish()
                    }
                    .addOnFailureListener {
                        mostrar(getString(R.string.error_network))
                        btnCrear.isEnabled = true
                    }
            }
            .addOnFailureListener { error ->
                val mensaje = when (error) {
                    is FirebaseAuthUserCollisionException -> getString(R.string.error_email_in_use)
                    is FirebaseAuthWeakPasswordException -> getString(R.string.error_weak_password)
                    is FirebaseAuthInvalidCredentialsException -> getString(R.string.error_invalid_email)
                    is FirebaseNetworkException -> getString(R.string.error_network)
                    else -> getString(R.string.error_network)
                }
                mostrar(mensaje)
                btnCrear.isEnabled = true
            }
    }

    /** Muestra un mensaje corto en pantalla. */
    private fun mostrar(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}