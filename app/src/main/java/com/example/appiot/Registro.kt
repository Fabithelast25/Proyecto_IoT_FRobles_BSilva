package com.example.appiot
import android.annotation.SuppressLint
import android.content.ContentValues
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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private lateinit var botonRegistrarUsuario: Button

lateinit var nuevo_nombres_usuario: EditText
lateinit var nuevo_apellidos_usuario: EditText
lateinit var nuevo_email_usuario: EditText
lateinit var nuevo_contrasenia_usuario: EditText
lateinit var nuevo_contrasenia2_usuario: EditText
lateinit var btn_registrar_nuevo_usuario: Button

class Registro : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lista)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        nuevo_nombres_usuario=findViewById(R.id.editTextNameUser)
        nuevo_apellidos_usuario=findViewById(R.id.editTextLastNameUser)
        nuevo_email_usuario=findViewById(R.id.editTextEmailUser)
        nuevo_contrasenia_usuario=findViewById(R.id.editTextPasswordUser)
        nuevo_contrasenia2_usuario=findViewById(R.id.editTextPassword2User)
        btn_registrar_nuevo_usuario=findViewById(R.id.buttonRegisterNewUser)

        btn_registrar_nuevo_usuario.setOnClickListener {
            if (nuevo_nombres_usuario.text.toString().isEmpty() ||
                nuevo_apellidos_usuario.text.toString().isEmpty() ||
                nuevo_email_usuario.text.toString().isEmpty() ||
                nuevo_contrasenia_usuario.text.toString().isEmpty() ||
                nuevo_contrasenia2_usuario.text.toString().isEmpty()
            ) {
                mostrarErrorCampoVacio()
                return@setOnClickListener
            }
            //Validar que nombres y apellidos contengan solo letras y espacios
            val regexSoloLetras = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")

            if (!regexSoloLetras.matches(nuevo_nombres_usuario.text.toString())) {
                mostrarErrorSoloLetras()
                return@setOnClickListener
            }

            if (!regexSoloLetras.matches(nuevo_apellidos_usuario.text.toString())) {
                mostrarErrorSoloLetras()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(nuevo_email_usuario.text.toString()).matches()) {
                mostrarErrorFormatoEmail()
                return@setOnClickListener
            }
            if (nuevo_contrasenia_usuario.text.toString() != nuevo_contrasenia2_usuario.text.toString()) {
                mostrarErrorComparacionContrasenia()
                return@setOnClickListener
            }
            if (!isValidPassword(nuevo_contrasenia_usuario.text.toString())) {
                mostrarErrorValidacionContrasenia()
                return@setOnClickListener
            }
            // Llamar a la función para registrar usuario
            registrarUsuario(nuevo_nombres_usuario.text.toString(), nuevo_apellidos_usuario.text.toString(), nuevo_email_usuario.text.toString(), nuevo_contrasenia2_usuario.text.toString()) { exito, mensaje ->

                if (mensaje.contains("ya existe", ignoreCase = true)) {
                    // Mostrar advertencia si el email ya está registrado
                    mostrarAdvertencia()

                } else if (exito) {
                    // Registro exitoso
                    procesoConCargandoYExito()

                    // Limpiar los campos
                    nuevo_nombres_usuario.text.clear()
                    nuevo_apellidos_usuario.text.clear()
                    nuevo_email_usuario.text.clear()
                    nuevo_contrasenia_usuario.text.clear()
                    nuevo_contrasenia2_usuario.text.clear()

                } else {
                    // Otro tipo de error
                    mostrarError()
                }
            }
        }

    }
    private fun registrarUsuario(nombres: String, apellidos: String, email: String, clave: String, callback: (Boolean, String) -> Unit) { Thread {
        try {
            val url = URL("http://52.91.135.89/registrar_usuario.php")
            val postData = "nombres=$nombres&apellidos=$apellidos&usuario=$email&clave=$clave"

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.outputStream.write(postData.toByteArray(Charsets.UTF_8))

            val response = conn.inputStream.bufferedReader().use { it.readText() }

            val json = JSONObject(response)
            val estado = json.getString("estado")
            val mensaje = json.getString("mensaje")
            val success = estado == "ok"

            Handler(Looper.getMainLooper()).post {
                callback(success, mensaje)
            }

        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                callback(false, "Error: ${e.message}")
            }
        }
    }.start()
    }
    fun isValidPassword(password: String): Boolean {
        val regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$"
        return password.matches(regex.toRegex())
    }

    private fun procesoConCargandoYExito() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando..."
        pDialog.setCancelable(false)
        pDialog.show()

        // Simular una operación (por ejemplo, guardar datos)
        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
            pDialog.titleText = "¡Éxito!"
            pDialog.contentText = "Se ha registrado correctamente."
            pDialog.confirmText = "Aceptar"
            pDialog.setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }, 3000)
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
    private fun mostrarErrorComparacionContrasenia() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Las contraseñas no coinciden.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarErrorValidacionContrasenia() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("La contraseña debe tener al menos 8 caracteres, incluyendo 1 letra minúscula, 1 letra mayúscula, 1 número y 1 carácter especial.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarAdvertencia() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Advertencia")
            .setContentText("El correo ingresado ya se encuentra registrado.")
            .setConfirmText("Entiendo")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarErrorSoloLetras() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Los campos de nombres y apellidos solo deben contener letras.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
    private fun mostrarError() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Error de conexión, por favor intente más tarde.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
}