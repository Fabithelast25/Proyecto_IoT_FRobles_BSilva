package com.example.appiot

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

lateinit var nombres_modificados: EditText
lateinit var apellidos_modificados: EditText
lateinit var correo_modificado: EditText
lateinit var btn_mod: Button
lateinit var btn_elim: Button

class Modificar_Eliminar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_modificar_eliminar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lista)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        nombres_modificados = findViewById(R.id.editTextNombresMod)
        apellidos_modificados = findViewById(R.id.editTextApellidosMod)
        correo_modificado = findViewById(R.id.editTextCorreoMod)
        btn_mod = findViewById(R.id.btn_modificar)
        btn_elim = findViewById(R.id.btn_eliminar)

        // Recibimos el ID del usuario a través del Intent
        val userId = intent.getStringExtra("USER_ID")

        if (userId != null && userId.isNotEmpty()) {
            // Cargar los datos del usuario
            cargarUsuario(userId, nombres_modificados, apellidos_modificados, correo_modificado)

            // Configurar el botón para modificar los datos
            btn_mod.setOnClickListener {
                try {
                    modificarUsuario(userId, nombres_modificados, apellidos_modificados, correo_modificado)
                } catch (e: Exception) {
                    e.printStackTrace()
                    mostrarErrorModificarUsuario()
                }
            }

            // Configurar el botón para eliminar el usuario
            btn_elim.setOnClickListener {
                try {
                    eliminarUsuario(userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                    mostrarErrorEliminarUsuario()
                }
            }
        } else {
            mostrarErrorID()
            finish()  // Cerrar la actividad si no se pasa un userId válido
        }

    }
    private fun cargarUsuario(userId: String, nombreEditText: EditText, apellidoEditText: EditText, emailEditText: EditText) {
        val url = "http://52.91.135.89/getUser.php?id=$userId"  // Asegúrate de tener la URL correcta

        val request = StringRequest(Request.Method.GET, url, { response ->
            try {
                val json = JSONArray(response)
                if (json.length() > 0) {
                    val usuario = json.getJSONObject(0)
                    nombreEditText.setText(usuario.getString("nombres"))
                    apellidoEditText.setText(usuario.getString("apellidos"))
                    emailEditText.setText(usuario.getString("usuario"))
                } else {
                    mostrarUsuarioNoEncontrado()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mostrarErrorProcesarDatos()
            }
        }, { error ->
            error.printStackTrace()
            mostrarErrorCargarDatos()
        })

        Volley.newRequestQueue(this).add(request)
    }

    private fun modificarUsuario(userId: String, nombreEditText: EditText, apellidoEditText: EditText, emailEditText: EditText) {
        // Mostrar el cuadro de confirmación antes de proceder
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Estás seguro?")
            .setContentText("Esta acción modificará los datos del usuario.")
            .setConfirmText("Sí")
            .setCancelText("No")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()  // Cerrar el cuadro de confirmación

                // Obtener los valores de los EditText
                val nombre = nombreEditText.text.toString().trim()
                val apellido = apellidoEditText.text.toString().trim()
                val email = emailEditText.text.toString().trim()

                // Verificar que no haya campos vacíos
                if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty()) {
                    mostrarErrorCampoVacio()
                    return@setConfirmClickListener
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email.toString()).matches()) {
                    mostrarErrorFormatoEmail()
                    return@setConfirmClickListener

                }

                // Mostrar el cuadro de cargando
                val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                pDialog.titleText = "Cargando..."
                pDialog.setCancelable(false)
                pDialog.show()

                // URL para la modificación
                val url = "http://52.91.135.89/modificarUsuario.php"
                val params = HashMap<String, String>()
                params["id"] = userId
                params["nombres"] = nombre
                params["apellidos"] = apellido
                params["usuario"] = email  // Cambié "email" por "usuario" porque en el PHP usas "usuario" como campo.

                // Realizar la solicitud POST
                val request = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        pDialog.dismissWithAnimation()  // Cerrar el cuadro de carga al recibir la respuesta

                        try {
                            // Parsear la respuesta JSON
                            val jsonResponse = JSONObject(response)
                            val estado = jsonResponse.getString("estado")
                            val mensaje = jsonResponse.getString("mensaje")

                            if (estado == "error") {
                                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error")
                                    .setContentText(mensaje)
                                    .setConfirmText("Aceptar")
                                    .show()
                            } else {
                                // Si el estado es "ok", los datos se modificaron con éxito
                                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("¡Éxito!")
                                    .setContentText("El usuario ha sido modificado correctamente.")
                                    .setConfirmText("Aceptar")
                                    .setConfirmClickListener { dialog ->
                                        dialog.dismissWithAnimation()

                                        // Devolver un resultado a la actividad anterior para actualizar la lista
                                        val intent = Intent()
                                        setResult(RESULT_OK, intent)  // Aquí notificamos que se modificó con éxito
                                        finish()  // Volver a la actividad anterior
                                    }
                                    .show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            pDialog.dismissWithAnimation()
                            mostrarErrorProcesarRespuesta()
                        }
                    },
                    Response.ErrorListener { error ->
                        pDialog.dismissWithAnimation()  // Cerrar el cuadro de carga en caso de error
                        mostrarErrorModificarUsuario()
                    }) {
                    override fun getParams(): Map<String, String> {
                        return params
                    }
                }

                // Agregar la solicitud a la cola de Volley
                Volley.newRequestQueue(this).add(request)
            }
            .setCancelClickListener { dialog ->
                dialog.dismissWithAnimation()  // Cierra el cuadro de confirmación si se cancela
            }
            .show()
    }
    private fun eliminarUsuario(userId: String) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("¿Estás seguro?")
            .setContentText("Esta acción eliminará los datos del usuario.")
            .setConfirmText("Sí")
            .setCancelText("No")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()  // Cerrar confirmación

                // Mostrar cuadro de carga
                val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                pDialog.titleText = "Eliminando..."
                pDialog.setCancelable(false)
                pDialog.show()

                val url = "http://52.91.135.89/eliminarUsuario.php?id=$userId"

                val request = StringRequest(Request.Method.GET, url, { response ->
                    pDialog.dismissWithAnimation()  // Cerrar carga

                    // Mostrar cuadro de éxito
                    SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("¡Éxito!")
                        .setContentText("El usuario ha sido eliminado correctamente.")
                        .setConfirmText("Aceptar")
                        .setConfirmClickListener { successDialog ->
                            successDialog.dismissWithAnimation()

                            //Enviar resultado a Listar.kt para actualizar lista
                            setResult(RESULT_OK)
                            finish()  // Cerrar esta actividad y volver a Listar.kt
                        }
                        .show()

                }, { error ->
                    pDialog.dismissWithAnimation()
                    mostrarErrorEliminarUsuario()
                })

                Volley.newRequestQueue(this).add(request)
            }
            .setCancelClickListener { dialog ->
                dialog.dismissWithAnimation()  // Si cancela, no hacer nada
            }
            .show()
    }
    private fun mostrarErrorEliminarUsuario() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Error al eliminar el usuario.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarErrorModificarUsuario() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Error al modificar el usuario.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarErrorCampoVacio() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Por favor, completa todos los campos obligatorios.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarErrorProcesarRespuesta() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Error al procesar la respuesta.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarErrorCargarDatos() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Error al cargar los datos.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarErrorProcesarDatos() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Error al procesar los datos.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarUsuarioNoEncontrado() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Error al procesar los datos.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarErrorID() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("No se proporcionó el ID del usuario.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarErrorFormatoEmail() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("El correo electrónico ingresado no es válido. Asegúrate de incluir '@' y un dominio correcto.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
}