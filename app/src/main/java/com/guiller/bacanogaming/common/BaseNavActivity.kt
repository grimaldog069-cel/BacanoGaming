package com.guiller.bacanogaming.common

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.guiller.bacanogaming.R

abstract class BaseNavActivity : AppCompatActivity() {

    abstract val menuRes: Int
    abstract val startItemId: Int
    abstract fun fragmentFor(itemId: Int): Fragment?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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

    fun mostrar(fragment: Fragment) {
        supportFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun abrirPantalla(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}