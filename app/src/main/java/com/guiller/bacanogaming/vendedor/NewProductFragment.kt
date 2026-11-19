package com.guiller.bacanogaming.vendedor

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.guiller.bacanogaming.R
import com.guiller.bacanogaming.common.ImagenRepository

/**
 * Formulario para crear (CREATE) o editar (UPDATE) un producto.
 * Si recibe el argumento "id", carga ese producto de Firestore para editarlo;
 * si no, crea uno nuevo. La foto puede venir de la cámara o de la galería.
 */
class NewProductFragment : Fragment(R.layout.fragment_new_product) {

    private val db = FirebaseFirestore.getInstance()
    private var productoId: String? = null
    private var imagenBase64: String = ""
    private lateinit var ivFoto: ImageView
    private lateinit var vistaFotoVacia: View

    /** Selector de imagen de la galería. */
    private val elegirDeGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val texto = ImagenRepository.uriABase64(requireContext(), uri)
                if (texto == null) mostrar(getString(R.string.msg_image_error)) else asignarImagen(texto)
            }
        }

    /** Cámara: devuelve una foto en tamaño reducido. */
    private val tomarFoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) asignarImagen(ImagenRepository.bitmapABase64(bitmap))
        }

    /** Enlaza las vistas, configura los botones y carga el producto si se está editando. */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivFoto = view.findViewById(R.id.ivFotoProducto)
        vistaFotoVacia = view.findViewById(R.id.llFotoVacia)
        productoId = arguments?.getString("id")

        view.findViewById<View>(R.id.ivVolverNuevo).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<View>(R.id.zonaSubirFoto).setOnClickListener { elegirOrigenDeFoto() }
        view.findViewById<Button>(R.id.btnPublicar).setOnClickListener { guardar(view) }

        if (productoId != null) {
            view.findViewById<TextView>(R.id.tvTituloProducto).setText(R.string.product_edit_title)
            view.findViewById<Button>(R.id.btnPublicar).setText(R.string.product_save_changes)
            cargarProducto(view, productoId!!)
        }
    }

    /** Pregunta si la foto se toma con la cámara o se elige de la galería. */
    private fun elegirOrigenDeFoto() {
        val opciones = arrayOf(
            getString(R.string.photo_source_camera),
            getString(R.string.photo_source_gallery)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.photo_source_title)
            .setItems(opciones) { _, cual ->
                if (cual == 0) tomarFoto.launch(null) else elegirDeGaleria.launch("image/*")
            }
            .show()
    }

    /** Guarda la imagen elegida y la muestra en el formulario. */
    private fun asignarImagen(texto: String) {
        imagenBase64 = texto
        ImagenRepository.base64ABitmap(texto)?.let { ivFoto.setImageBitmap(it) }
        ivFoto.visibility = View.VISIBLE
        vistaFotoVacia.visibility = View.GONE
    }

    /** Lee el producto de Firestore y llena el formulario para editarlo. */
    private fun cargarProducto(vista: View, id: String) {
        db.collection("products").document(id).get().addOnSuccessListener { doc ->
            if (!isAdded || !doc.exists()) return@addOnSuccessListener
            vista.findViewById<EditText>(R.id.etNombreProducto).setText(doc.getString("nombre"))
            vista.findViewById<EditText>(R.id.etDescripcion).setText(doc.getString("descripcion"))
            vista.findViewById<EditText>(R.id.etPrecio).setText((doc.getLong("precio") ?: 0L).toString())
            vista.findViewById<EditText>(R.id.etStock).setText((doc.getLong("stock") ?: 0L).toString())
            vista.findViewById<EditText>(R.id.etColor).setText(doc.getString("color"))
            vista.findViewById<EditText>(R.id.etModelo).setText(doc.getString("modelo"))
            val categorias = resources.getStringArray(R.array.product_categories)
            val posicion = categorias.indexOf(doc.getString("categoria"))
            if (posicion >= 0) vista.findViewById<Spinner>(R.id.spCategoria).setSelection(posicion)
            val foto = doc.getString("imagen").orEmpty()
            if (foto.isNotEmpty()) asignarImagen(foto)
        }
    }

    /** Valida el formulario y crea o actualiza el producto en Firestore. */
    private fun guardar(vista: View) {
        val nombre = vista.findViewById<EditText>(R.id.etNombreProducto).text.toString().trim()
        val descripcion = vista.findViewById<EditText>(R.id.etDescripcion).text.toString().trim()
        val precio = vista.findViewById<EditText>(R.id.etPrecio).text.toString().toLongOrNull()
        val stock = vista.findViewById<EditText>(R.id.etStock).text.toString().toLongOrNull()
        val color = vista.findViewById<EditText>(R.id.etColor).text.toString().trim()
        val modelo = vista.findViewById<EditText>(R.id.etModelo).text.toString().trim()
        val categoria = vista.findViewById<Spinner>(R.id.spCategoria).selectedItem?.toString().orEmpty()

        if (nombre.isEmpty() || precio == null || stock == null) {
            mostrar(getString(R.string.error_product_fields))
            return
        }

        val boton = vista.findViewById<Button>(R.id.btnPublicar)
        boton.isEnabled = false

        val datos = hashMapOf<String, Any>(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "categoria" to categoria,
            "precio" to precio,
            "stock" to stock,
            "color" to color,
            "modelo" to modelo,
            "imagen" to imagenBase64
        )

        val id = productoId
        val tarea = if (id != null) {
            // UPDATE: solo cambia los campos del formulario
            db.collection("products").document(id).update(datos)
        } else {
            // CREATE: producto nuevo, activo y a nombre del vendedor
            datos["activo"] = true
            datos["vendedorId"] = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            datos["fecha"] = FieldValue.serverTimestamp()
            db.collection("products").add(datos)
        }

        tarea.addOnSuccessListener {
            if (!isAdded) return@addOnSuccessListener
            mostrar(getString(R.string.msg_product_saved))
            parentFragmentManager.popBackStack()
        }.addOnFailureListener {
            boton.isEnabled = true
            mostrar(getString(R.string.msg_product_error))
        }
    }

    /** Muestra un mensaje corto en pantalla. */
    private fun mostrar(mensaje: String) {
        context?.let { Toast.makeText(it, mensaje, Toast.LENGTH_LONG).show() }
    }
}