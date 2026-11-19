package com.guiller.bacanogaming.common

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.guiller.bacanogaming.R

/**
 * Activity base de los tres roles. Carga el layout con el contenedor de pantallas
 * y la barra de navegación inferior; cada rol define su menú, su pantalla inicial
 * y qué fragment corresponde a cada opción del menú.
 */
abstract class BaseNavActivity : AppCompatActivity() {

    /** Menú (XML) de la barra inferior del rol. */
    abstract val menuRes: Int

    /** Opción del menú que se muestra al abrir la pantalla. */
    abstract val startItemId: Int

    /** Devuelve el fragment que corresponde a una opción del menú, o null si no existe. */
    abstract fun fragmentFor(itemId: Int): Fragment?

    /** Prepara la pantalla, carga el menú del rol y cambia de fragment al tocar cada opción. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Evita que el contenido quede debajo de las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val nav = findViewById<BottomNavigationView>(R.id.bottomNav)
        nav.menu.clear()
        nav.inflateMenu(menuRes)

        nav.setOnItemSelectedListener { item ->
            val fragment = fragmentFor(item.itemId)
            if (fragment != null) {
                mostrar(fragment)
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            nav.selectedItemId = startItemId
        }
    }

    /** Muestra una sección principal y borra el historial de pantallas internas. */
    fun mostrar(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /** Abre una pantalla interna encima de la actual, permitiendo volver con el botón atrás. */
    fun abrirPantalla(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}