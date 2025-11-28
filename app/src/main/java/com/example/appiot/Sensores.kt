package com.example.appiot

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

lateinit var temp: TextView
lateinit var hum: TextView
lateinit var imagenTemp: ImageView
lateinit var datos: RequestQueue

lateinit var cameraManager: CameraManager
var cameraId: String? = null
var isFlashOn = false

lateinit var iconoFlash: ImageView
var iconoEncendido = false
private val handler = android.os.Handler()
lateinit var runnable: Runnable

class Sensores : AppCompatActivity() {

    private val PREFS_NAME = "AppIoTPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sensores)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.lista)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        temp = findViewById(R.id.txt_temp)
        hum = findViewById(R.id.txt_humedad)
        imagenTemp = findViewById(R.id.imagen_temp)
        datos = Volley.newRequestQueue(this)

        runnable = object : Runnable {
            override fun run() {
                obtenerDatos()           // Llamas a tu función de Volley
                handler.postDelayed(this, 1000) // 1000 ms = 1 segundo
            }
        }
        handler.post(runnable)

        // Cargar estados guardados
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isFlashOn = prefs.getBoolean("flash_state", false)
        iconoEncendido = prefs.getBoolean("icon_state", false)

        // --- Icono que solo cambia de imagen ---
        iconoFlash = findViewById(R.id.iconolightButton)
        actualizarIconoExtra()

        iconoFlash.setOnClickListener {
            iconoEncendido = !iconoEncendido
            actualizarIconoExtra()
            guardarEstados()
        }

        // --- Configuración de la linterna real ---
        val flashlightButton = findViewById<ImageView>(R.id.flashlightButton)
        updateIcon(flashlightButton)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        }

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        flashlightButton.setOnClickListener {
            toggleFlashlight()
            updateIcon(flashlightButton)
            guardarEstados()
        }

        obtenerDatos()
    }

    private fun obtenerDatos() {
        val url = "https://www.pnk.cl/muestra_datos.php"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response: JSONObject ->
                try {
                    temp.text = "${response.getString("temperatura")} °C"
                    hum.text = "${response.getString("humedad")} %"
                    val valor = response.getString("temperatura").toFloat()
                    cambiarImagen(valor)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error: VolleyError ->
                error.printStackTrace()
            }
        )
        datos.add(request)
    }

    private fun cambiarImagen(valor: Float) {
        if (valor > 20) {
            imagenTemp.setImageResource(R.drawable.temperaturacalor)
        } else {
            imagenTemp.setImageResource(R.drawable.temperaturafrio)
        }
    }

    private fun toggleFlashlight() {
        try {
            if (cameraId != null) {
                isFlashOn = !isFlashOn
                cameraManager.setTorchMode(cameraId!!, isFlashOn)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun updateIcon(flashlightButton: ImageView) {
        val icon = if (isFlashOn) R.drawable.flashlight_on else R.drawable.flashlight_off
        flashlightButton.setImageResource(icon)
    }

    private fun actualizarIconoExtra() {
        val icon = if (iconoEncendido) R.drawable.flash_on else R.drawable.flash_off
        iconoFlash.setImageResource(icon)
    }

    private fun guardarEstados() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("flash_state", isFlashOn)
        editor.putBoolean("icon_state", iconoEncendido)
        editor.apply()
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
