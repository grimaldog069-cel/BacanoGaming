package com.guiller.bacanogaming.comprador

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.*

/** Muestra un mensaje corto desde cualquier fragment. */
private fun Fragment.avisar(texto: String) {
    context?.let { Toast.makeText(it, texto, Toast.LENGTH_LONG).show() }
}

/** Pantalla de inicio de la tienda (diseño). */
class HomeFragment : Fragment(R.layout.fragment_home)

/**
 * Catálogo: lista en tiempo real los productos activos de Firestore.
 * Al tocar uno abre su detalle.
 */
class CatalogFragment : Fragment(R.layout.fragment_catalog) {

    private var escucha: ListenerRegistration? = null

    /** Escucha la colección "products" y dibuja la cuadrícula de productos. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lista = view.findViewById<RecyclerView>(R.id.rvProductos)

        escucha = FirebaseFirestore.getInstance().collection("products")
            .whereEqualTo("activo", true)
            .addSnapshotListener { instantanea, error ->
                if (error != null || instantanea == null) return@addSnapshotListener
                val productos = instantanea.documents
                    .map { it.aArticulo() }
                    .sortedBy { it.nombre.lowercase() }
                lista.adapter = ListaAdapter(
                    productos,
                    R.layout.item_catalog_product,
                    bind = { v, a ->
                        ImagenRepository.mostrarEn(v.findViewById<ImageView>(R.id.ivProductoCatalogo), a.imagen)
                        v.findViewById<TextView>(R.id.tvNombreProducto).text = a.nombre
                        v.findViewById<TextView>(R.id.tvPrecioProducto).text = formatoPrecio(a.precio)
                    },
                    onClick = { a ->
                        val detalle = ProductDetailFragment()
                        detalle.arguments = bundleOf("id" to a.id)
                        (requireActivity() as BaseNavActivity).abrirPantalla(detalle)
                    }
                )
            }
    }

    /** Deja de escuchar Firestore cuando la pantalla se destruye. */
    override fun onDestroyView() {
        escucha?.remove()
        super.onDestroyView()
    }
}

/**
 * Detalle de un producto: carga sus datos de Firestore usando el id recibido
 * y permite agregarlo al carrito.
 */
class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private var articulo: Articulo? = null

    /** Carga el producto, muestra sus datos y configura los botones. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.ivVolver).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<View>(R.id.btnAgregarCarrito).setOnClickListener {
            val a = articulo ?: return@setOnClickListener
            if (Carrito.agregar(a)) {
                avisar(getString(R.string.msg_added_cart))
            } else {
                avisar(getString(R.string.msg_out_of_stock))
            }
        }

        val id = arguments?.getString("id") ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("products").document(id).get().addOnSuccessListener { doc ->
            if (!isAdded || !doc.exists()) return@addOnSuccessListener
            val a = doc.aArticulo()
            articulo = a

            ImagenRepository.mostrarEn(view.findViewById<ImageView>(R.id.ivProductoDetalle), a.imagen)
            view.findViewById<TextView>(R.id.tvNombreDetalle).text = a.nombre
            view.findViewById<TextView>(R.id.tvPrecioDetalle).text = formatoPrecio(a.precio)
            view.findViewById<TextView>(R.id.tvStockDetalle).text =
                getString(R.string.detail_stock_format, a.stock.toInt())

            val partes = mutableListOf<String>()
            if (a.categoria.isNotEmpty()) partes.add("Categoría: ${a.categoria}")
            if (a.descripcion.isNotEmpty()) partes.add(a.descripcion)
            doc.getString("color")?.takeIf { it.isNotBlank() }?.let { partes.add("Color: $it") }
            doc.getString("modelo")?.takeIf { it.isNotBlank() }?.let { partes.add("Modelo: $it") }
            view.findViewById<TextView>(R.id.tvEspecificaciones).text = partes.joinToString("\n")

            // Nombre del vendedor, tomado de su perfil
            db.collection("users").document(a.vendedorId).get().addOnSuccessListener { vendedor ->
                if (isAdded) {
                    view.findViewById<TextView>(R.id.tvVendedorDetalle).text =
                        getString(R.string.detail_vendor_format, vendedor.getString("nombre").orEmpty())
                }
            }
        }
    }
}

/**
 * Carrito de compras: muestra los productos agregados, permite cambiar
 * cantidades o quitarlos y calcula subtotal, envío y total.
 */
class CartFragment : Fragment(R.layout.fragment_cart) {

    /** Dibuja el carrito y configura el botón para continuar al pago. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnContinuarPago).setOnClickListener {
            if (Carrito.estaVacio()) {
                avisar(getString(R.string.msg_cart_empty))
            } else {
                (requireActivity() as BaseNavActivity).abrirPantalla(CheckoutFragment())
            }
        }
        refrescar(view)
    }

    /** Vuelve a dibujar la lista y los totales con el estado actual del carrito. */
    private fun refrescar(raiz: View) {
        val items = Carrito.lista()
        raiz.findViewById<RecyclerView>(R.id.rvCarrito).adapter = ListaAdapter(
            items,
            R.layout.item_cart,
            bind = { v, item ->
                val a = item.articulo
                ImagenRepository.mostrarEn(v.findViewById<ImageView>(R.id.ivProductoCarrito), a.imagen)
                v.findViewById<TextView>(R.id.tvNombreCarrito).text = a.nombre
                v.findViewById<TextView>(R.id.tvPrecioCarrito).text = formatoPrecio(a.precio * item.cantidad)
                v.findViewById<TextView>(R.id.tvCantidad).text = item.cantidad.toString()

                v.findViewById<View>(R.id.tvMas).setOnClickListener {
                    if (Carrito.cambiarCantidad(item, 1)) {
                        refrescar(raiz)
                    } else {
                        avisar(getString(R.string.msg_out_of_stock))
                    }
                }
                v.findViewById<View>(R.id.tvMenos).setOnClickListener {
                    Carrito.cambiarCantidad(item, -1)
                    refrescar(raiz)
                }
                v.findViewById<View>(R.id.tvQuitar).setOnClickListener {
                    Carrito.quitar(item)
                    refrescar(raiz)
                }
            }
        )
        raiz.findViewById<View>(R.id.tvCarritoVacio).visibility =
            if (items.isEmpty()) View.VISIBLE else View.GONE
        raiz.findViewById<TextView>(R.id.tvSubtotal).text = formatoPrecio(Carrito.subtotal())
        raiz.findViewById<TextView>(R.id.tvEnvio).text = formatoPrecio(Carrito.envio())
        raiz.findViewById<TextView>(R.id.tvTotal).text = formatoPrecio(Carrito.total())
    }
}

/**
 * Pago: pide la dirección y el método de pago, muestra el resumen y al pulsar
 * "Pagar" registra la orden en Firestore.
 */
class CheckoutFragment : Fragment(R.layout.fragment_checkout) {

    /** Muestra el resumen del carrito y configura el botón de pagar. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.ivVolverPago).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<TextView>(R.id.tvSubtotalPago).text = formatoPrecio(Carrito.subtotal())
        view.findViewById<TextView>(R.id.tvEnvioPago).text = formatoPrecio(Carrito.envio())
        view.findViewById<TextView>(R.id.tvTotalPago).text = formatoPrecio(Carrito.total())

        val etDireccion = view.findViewById<EditText>(R.id.etDireccion)
        val grupoPago = view.findViewById<RadioGroup>(R.id.rgMetodoPago)
        val botonPagar = view.findViewById<View>(R.id.btnPagar)

        botonPagar.setOnClickListener {
            val direccion = etDireccion.text.toString().trim()
            val metodo = when (grupoPago.checkedRadioButtonId) {
                R.id.rbPse -> "PSE"
                R.id.rbBilletera -> "Billetera digital"
                else -> "Tarjeta"
            }
            when {
                Carrito.estaVacio() -> avisar(getString(R.string.msg_cart_empty))
                direccion.isEmpty() -> avisar(getString(R.string.msg_address_required))
                else -> {
                    botonPagar.isEnabled = false
                    OrdenRepository.crear(
                        direccion,
                        metodo,
                        alExito = {
                            if (isAdded) {
                                avisar(getString(R.string.msg_order_created))
                                // Lleva al historial de pedidos
                                requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
                                    .selectedItemId = R.id.nav_orders
                            }
                        },
                        alFallar = {
                            if (isAdded) {
                                botonPagar.isEnabled = true
                                avisar(getString(R.string.msg_order_error))
                            }
                        }
                    )
                }
            }
        }
    }
}

/** Historial de pedidos del comprador, leído en tiempo real de Firestore. */
class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private var escucha: ListenerRegistration? = null

    /** Escucha las órdenes del comprador y las dibuja, las más recientes primero. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val lista = view.findViewById<RecyclerView>(R.id.rvPedidos)

        escucha = FirebaseFirestore.getInstance().collection("orders")
            .whereEqualTo("compradorId", uid)
            .addSnapshotListener { instantanea, error ->
                if (error != null || instantanea == null) return@addSnapshotListener
                val ordenes = instantanea.documents
                    .map { it.aOrden() }
                    .sortedByDescending { it.fecha?.time ?: Long.MAX_VALUE }
                lista.adapter = ListaAdapter(ordenes, R.layout.item_order, bind = { v, o ->
                    v.findViewById<TextView>(R.id.tvPedidoId).text = "#" + o.id.take(6).uppercase()
                    aplicarEstado(v.findViewById(R.id.tvEstado), estadoANumero(o.estado))
                    v.findViewById<TextView>(R.id.tvFechaPedido).text = formatoFecha(o.fecha)
                    v.findViewById<TextView>(R.id.tvItemsPedido).text =
                        o.items.joinToString("\n") { "${it.cantidad} x ${it.nombre}" }
                    v.findViewById<TextView>(R.id.tvTotalPedido).text =
                        getString(R.string.order_total_format, formatoPrecio(o.total))
                })
            }
    }

    /** Deja de escuchar Firestore cuando la pantalla se destruye. */
    override fun onDestroyView() {
        escucha?.remove()
        super.onDestroyView()
    }
}

/** Perfil del comprador con el botón de cerrar sesión. */
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnCerrarSesion).setOnClickListener {
            Sesion.cerrar(requireActivity())
        }
    }
}