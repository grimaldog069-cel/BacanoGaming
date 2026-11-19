package com.guiller.bacanogaming.common

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.guiller.bacanogaming.R

/** Pantalla de bienvenida: muestra el logo dos segundos y pasa al inicio de sesión. */
class SplashActivity : AppCompatActivity() {

    /** Muestra el splash y programa el cambio automático a la pantalla de Login. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2000)
    }
}