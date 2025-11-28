package com.example.appiot

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException


lateinit var editTextEmail: EditText
lateinit var editTextCodigo: EditText
lateinit var btnRecuperar: Button
lateinit var textTimeCounter: TextView
lateinit var btnVerificarCodigo: Button

lateinit var textIngresar: TextView
lateinit var dato_usuarios: RequestQueue

private var timer: CountDownTimer? = null
class Recuperar : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lista)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextEmail=findViewById(R.id.editTextTextEmailAddress2)
        editTextCodigo=findViewById(R.id.editTextNumber7)
        btnRecuperar=findViewById(R.id.buttonRecoverPasssword)
        textTimeCounter=findViewById(R.id.textViewTimer)
        btnVerificarCodigo=findViewById(R.id.buttonVerifyCode)
        textIngresar=findViewById(R.id.textView8)
        dato_usuarios= Volley.newRequestQueue(this);

        btnRecuperar.setOnClickListener {
            if (editTextEmail.text.isEmpty()) {
                mostrarErrorCampoVacio()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(editTextEmail.text.toString()).matches()) {
                mostrarErrorFormatoEmail()
                return@setOnClickListener

            }

            consultarDatoEmail(editTextEmail.text.toString(), btnVerificarCodigo)
        }
    }

    private fun enviarCodigo(email: String) {
        val url = "http://52.91.135.89/enviar_codigo.php"
        val postRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.contains("success")) {
                    iniciarCuentaRegresiva(textTimeCounter,btnRecuperar, btnVerificarCodigo, textIngresar,editTextCodigo )
                }
            },
            { error ->
                mostrarError()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("email" to email)
            }
        }
        Volley.newRequestQueue(this).add(postRequest)
    }

    private fun validarCodigo(email: String, codigo: String) {
        val url = "http://52.91.135.89/validar_codigo.php"
        val postRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                if (response.contains("success")) {
                    // Código correcto, abrir Claves
                    mostrarCargando()
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Código correcto, abrir Claves
                        val intent = Intent(this, Claves::class.java)
                        intent.putExtra("email", email) // opcional: pasar email a la siguiente actividad
                        startActivity(intent)
                    }, 3000)
                } else {
                    mostrarErrorCodigo()
                }
            },
            { error ->
                mostrarError()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "email" to email,
                    "codigo" to codigo
                )
            }
        }
        Volley.newRequestQueue(this).add(postRequest)
    }

    private fun iniciarCuentaRegresiva(timerView: TextView, btnValidar: Button, btnVerificarCodigo: Button, textIngresar: TextView, code: TextView) {
        btnValidar.isEnabled = false
        timerView.visibility = TextView.VISIBLE
        btnVerificarCodigo.visibility = Button.VISIBLE
        textIngresar.visibility = TextView.VISIBLE
        code.visibility = TextView.VISIBLE

        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segundosRestantes = millisUntilFinished / 1000
                timerView.text = "Tiempo restante: ${segundosRestantes}s"
            }

            override fun onFinish() {
                timerView.text = "El código ha expirado"
                btnValidar.isEnabled = false
            }
        }.start()
    }

    fun consultarDatoEmail(usu: String, verifyCode: Button) {
        val url = "http://52.91.135.89/consultaemail.php?usu=$usu"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val estado = response.getString("estado")
                    if (estado == "0") {
                        procesoConCargandoYFracaso()
                    } else {
                        procesoConCargandoYExito()
                        enviarCodigo(usu)
                        verifyCode.setOnClickListener {
                            val codigoIngresado = editTextCodigo.text.toString().trim()
                            validarCodigo(usu,codigoIngresado)
                        }

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                mostrarError()
            }
        )
        dato_usuarios.add(request)
    }

    private fun procesoConCargandoYExito() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando..."
        pDialog.setCancelable(false)
        pDialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
            pDialog.titleText = "¡Éxito!"
            pDialog.contentText = "El código fue enviado a su correo."
            pDialog.confirmText = "Aceptar"
            pDialog.setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
        }, 3000)
    }


    private fun procesoConCargandoYFracaso() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando..."
        pDialog.setCancelable(false)
        pDialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
            pDialog.titleText = "¡Error!"
            pDialog.contentText = "El correo ingresado no es correcto o no existe."
            pDialog.confirmText = "Aceptar"
            pDialog.setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
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
    private fun mostrarErrorCodigo() {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("El código ingresado es incorrecto.")
            .setConfirmText("Cerrar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }

    private fun mostrarCargando()
    {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Cargando..."
        pDialog.setCancelable(false)
        pDialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            pDialog.dismissWithAnimation()
        }, 3000) // 3000 milisegundos = 3 segundos
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
