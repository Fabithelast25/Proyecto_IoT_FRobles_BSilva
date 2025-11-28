package com.example.appiot

import android.annotation.SuppressLint
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

lateinit var botonRegistrar: Button
lateinit var nuevo_nombres: EditText
lateinit var nuevo_apellidos: EditText
lateinit var nuevo_email: EditText
lateinit var nuevo_contrasenia: EditText
lateinit var nuevo_contrasenia2: EditText



class Ingreso : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ingreso)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lista)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        botonRegistrar=findViewById(R.id.buttonRegisterNewUser)
        nuevo_nombres=findViewById(R.id.editTextNombres)
        nuevo_apellidos=findViewById(R.id.editTextApellidos)
        nuevo_email=findViewById(R.id.editTextEmail)
        nuevo_contrasenia=findViewById(R.id.editTextPasswordRegister)
        nuevo_contrasenia2=findViewById(R.id.editTextPasswordRegister2)

        botonRegistrar.setOnClickListener {
            if (nuevo_nombres.text.toString().isEmpty() ||
                nuevo_apellidos.text.toString().isEmpty() ||
                nuevo_email.text.toString().isEmpty() ||
                nuevo_contrasenia.text.toString().isEmpty() ||
                nuevo_contrasenia2.text.toString().isEmpty()
            ) {
                mostrarErrorCampoVacio()
                return@setOnClickListener
            }
            val regexSoloLetras = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")

            if (!regexSoloLetras.matches(nuevo_nombres.text.toString())) {
                mostrarErrorSoloLetras()
                return@setOnClickListener
            }

            if (!regexSoloLetras.matches(nuevo_apellidos.text.toString())) {
                mostrarErrorSoloLetras()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(nuevo_email.text.toString()).matches()) {
                mostrarErrorFormatoEmail()
                return@setOnClickListener
            }
            if (nuevo_contrasenia.text.toString() != nuevo_contrasenia2.text.toString()) {
                mostrarErrorComparacionContrasenia()
                return@setOnClickListener
            }
            if (!isValidPassword(nuevo_contrasenia.text.toString())) {
                mostrarErrorValidacionContrasenia()
                return@setOnClickListener
            }
            // Llamar a la función para registrar usuario
            registrarUsuario(nuevo_nombres.text.toString(), nuevo_apellidos.text.toString(), nuevo_email.text.toString(), nuevo_contrasenia2.text.toString()) { exito, mensaje ->

            if (mensaje.contains("ya existe", ignoreCase = true)) {
                // Mostrar advertencia si el email ya está registrado
                mostrarAdvertencia()

            } else if (exito) {
                // Registro exitoso
                procesoConCargandoYExito()

                // Limpiar los campos
                nuevo_nombres.text.clear()
                nuevo_apellidos.text.clear()
                nuevo_email.text.clear()
                nuevo_contrasenia.text.clear()
                nuevo_contrasenia2.text.clear()

            } else {
                // Otro tipo de error
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
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

}




