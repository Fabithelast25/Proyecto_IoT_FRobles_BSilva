package com.example.appiot

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

lateinit var username: EditText
lateinit var password: EditText
lateinit var btnEnter: Button

lateinit var btnRegister: Button
lateinit var datos_usuarios: RequestQueue


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lista)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        username=findViewById(R.id.userName)
        password=findViewById(R.id.password)
        btnEnter=findViewById(R.id.buttonEnter)
        btnRegister=findViewById(R.id.buttonRegister)
        datos_usuarios= Volley.newRequestQueue(this);

        btnEnter.setOnClickListener()
        {
            consultarDatos(username.getText().toString(),password.getText().toString());
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, Ingreso::class.java)
            startActivity(intent)
        }

        val tvLinkPantalla: TextView = findViewById(R.id.RecuperarClave)
        tvLinkPantalla.setOnClickListener {
            val intent = Intent(this, Recuperar::class.java)
            startActivity(intent)
        }
    }

    fun consultarDatos(usu: String, pass: String) {
        if (usu.isEmpty() || pass.isEmpty()) {
            mostrarErrorCampoVacio()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(usu).matches()) {
            mostrarErrorFormatoEmail()
            return
        }
        val encodedPassword = try {
            URLEncoder.encode(pass, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            ""
        }
        val url = "http://52.91.135.89/consultadatos.php?usu=$usu&pass=$encodedPassword"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val estado = response.getString("estado")
                    if (estado == "0") {
                        procesoConCargandoYFracaso()
                    } else {
                        procesoConCargandoYExito()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                mostrarError()
            }
        )
        datos_usuarios.add(request)

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
            pDialog.contentText = "Se ha iniciado sesión correctamente."
            pDialog.confirmText = "Aceptar"
            pDialog.setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                val intent = Intent(this, Principal::class.java)
                startActivity(intent)
            }
        }, 3000) // Espera 3 segundos simulando un proceso
    }

    private fun procesoConCargandoYFracaso() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando..."
        pDialog.setCancelable(false)
        pDialog.show()

        // Simular una operación (por ejemplo, guardar datos)
        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
            pDialog.titleText = "¡Error!"
            pDialog.contentText = "Los datos ingresados son incorrectos."
            pDialog.confirmText = "Aceptar"
            pDialog.setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
        }, 3000) // Espera 3 segundos simulando un proceso
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