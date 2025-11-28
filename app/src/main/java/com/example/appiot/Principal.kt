package com.example.appiot

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

lateinit var fecha_ahora : TextView
val mHandler = Handler(Looper.getMainLooper())
class Principal : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lista)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fecha_ahora = findViewById(R.id.textFechaAhora) // Asegúrate de que este ID esté en el XML
        mHandler.post(refrescar)

        val btn: Button = findViewById(R.id.buttonCRUDUser)
        btn.setOnClickListener {
            val intent: Intent = Intent(this, CRUD_Usuario:: class.java)
            startActivity(intent)
        }
        val btn2: Button = findViewById(R.id.buttonViewSensorData)
        btn2.setOnClickListener {
            val intent: Intent = Intent(this, Sensores:: class.java)
            startActivity(intent)
        }
        val btn3: Button = findViewById(R.id.buttonViewDesarrolladores)
        btn3.setOnClickListener {
            val intent: Intent = Intent(this, Desarrolladores:: class.java)
            startActivity(intent)
        }

    }
    fun fechahora(): String {
        val c: Calendar = Calendar.getInstance()
        val sdf: SimpleDateFormat = SimpleDateFormat("dd-MM-YYYY, HH:mm:ss a")
        val strDate: String = sdf.format(c.getTime())
        return "Fecha/Hora: $strDate"
    }
    private val refrescar = object : Runnable {
        override fun run() {
            fecha_ahora?.text = fechahora() // Asignación de la hora actual
            mHandler.postDelayed(this, 1000) // Se vuelve a ejecutar en 1 segundo
        }
    }
}