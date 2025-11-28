package com.example.appiot

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

lateinit var contrasenia: EditText
lateinit var contrasenia2: EditText
lateinit var cambiar_contrasenia: Button

class Claves : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_claves)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lista)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        contrasenia = findViewById(R.id.editTextContrasenia)
        contrasenia2 = findViewById(R.id.editTextContrasenia2)
        cambiar_contrasenia = findViewById(R.id.buttonChangePassword)
        val intent = intent
        val email = intent.getStringExtra("email")

        cambiar_contrasenia.setOnClickListener {
            // Validar que las contraseñas no estén vacías
            if (contrasenia.text.toString().isEmpty() || contrasenia2.text.toString().isEmpty()) {
                mostrarErrorCampoVacio()
                return@setOnClickListener
            }

            // Validar que las contraseñas sean iguales
            if (contrasenia.text.toString() != contrasenia2.text.toString()) {
                mostrarErrorComparacionContrasenia()
                return@setOnClickListener
            }

            // Validar la complejidad de la contraseña
            if (!isValidPassword(contrasenia.text.toString())) {
                mostrarErrorValidacionContrasenia()
                return@setOnClickListener
            }

            // Aquí ya puedes guardar la nueva contraseña (por ejemplo, actualizar en la base de datos)
            cambiarContrasenia(email.toString(), contrasenia2.text.toString())
        }
    }

    fun cambiarContrasenia(email: String, contrasenia_nueva: String){
        val url = "http://52.91.135.89/cambiar_contrasenia.php"

        val queue = Volley.newRequestQueue(this)

        // Datos que enviarás al servidor
        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = contrasenia_nueva

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                procesoConCargandoYExitoCambioContrasenia()
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): Map<String, String> = params
        }
        queue.add(request)
    }
    fun isValidPassword(password: String): Boolean {
        // Expresión regular para validar la contraseña:
        // - Al menos 8 caracteres
        // - Al menos 1 letra minúscula
        // - Al menos 1 letra mayúscula
        // - Al menos 1 número
        // - Al menos 1 carácter especial (por ejemplo: !@#$%^&*)
        val regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>]).{8,}$"
        return password.matches(regex.toRegex())
    }

    private fun mostrarErrorCampoVacio() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("Las contraseñas no pueden estar vacías.")
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
    private fun procesoConCargandoYExitoCambioContrasenia() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando..."
        pDialog.setCancelable(false)
        pDialog.show()

        // Simular una operación (por ejemplo, guardar datos)
        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
            pDialog.titleText = "¡Éxito!"
            pDialog.contentText = "Contraseña actualizada con éxito."
            pDialog.confirmText = "Aceptar"
            pDialog.setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }, 3000) // Espera 3 segundos simulando un proceso
    }
}