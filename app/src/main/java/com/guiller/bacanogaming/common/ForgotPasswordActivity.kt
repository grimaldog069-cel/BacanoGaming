package com.guiller.bacanogaming.common

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.guiller.bacanogaming.R

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etCorreo = findViewById<EditText>(R.id.etCorreoRecuperar)

        findViewById<Button>(R.id.btnEnviarEnlace).setOnClickListener {
            if (etCorreo.text.isBlank()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.msg_reset_link_sent, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        findViewById<TextView>(R.id.tvVolverLogin).setOnClickListener { finish() }
    }
}