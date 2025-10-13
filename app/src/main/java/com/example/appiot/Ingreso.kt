package com.example.appiot

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

private lateinit var botonRegistrar: Button

class Ingreso : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ingreso)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        botonRegistrar=findViewById(R.id.buttonUserRegister)

        botonRegistrar.setOnClickListener {
            procesoConCargandoYExito()
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
            pDialog.titleText = "¡Registrado!"
            pDialog.contentText = "Se ha registrado con éxito."
            pDialog.confirmText = "Aceptar"
            pDialog.setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }, 3000) // Espera 3 segundos simulando un proceso
    }

}