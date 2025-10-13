package com.example.appiot

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog

private lateinit var botonIniciar: Button
private lateinit var botonRegistrar: Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        botonIniciar=findViewById(R.id.buttonEnter)
        botonRegistrar=findViewById(R.id.buttonRegister)

        botonIniciar.setOnClickListener {
            procesoConCargandoYExito()
        }

        botonRegistrar.setOnClickListener {
            val intent = Intent(this, Ingreso::class.java)
            startActivity(intent)
        }


        val tvLinkPantalla: TextView = findViewById(R.id.RecuperarClave)
        tvLinkPantalla.setOnClickListener {
            val intent = Intent(this, Recuperar::class.java) // OtraActivity es la pantalla a la que quieres navegar
            startActivity(intent)
        }
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
}