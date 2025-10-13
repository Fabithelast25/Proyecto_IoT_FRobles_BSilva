package com.example.appiot

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Principal : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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
    }
}