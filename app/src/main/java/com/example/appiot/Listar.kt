package com.example.appiot

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import java.text.Normalizer

class Listar : AppCompatActivity() {

    private lateinit var listado: ListView
    private lateinit var listaUsuario: ArrayList<String>
    private lateinit var listaFiltrada: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dato: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listar)

        listado = findViewById(R.id.lista)
        dato = Volley.newRequestQueue(this)

        listaUsuario = ArrayList()
        listaFiltrada = ArrayList()

        cargarLista()

        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim()?.lowercase() ?: ""
                filtrarDatos(query)
                return true
            }
        })

        listado.setOnItemClickListener { parent, view, position, id ->
            val selectedUser = listaFiltrada[position]  // Obtener el nombre completo del usuario

            val userParts = selectedUser.split(" ")
            if (userParts.size > 1) {
                val userId = userParts[0]  // Suponiendo que el ID está en la primera parte
                openEditUserActivity(userId)
            } else {
                Toast.makeText(this, "Error: No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Esta es la función que abre la actividad de modificación de usuario
    private fun openEditUserActivity(userId: String) {
        val intent = Intent(this, Modificar_Eliminar::class.java)
        intent.putExtra("USER_ID", userId)  // Pasamos el ID del usuario
        startActivityForResult(intent, 1)  // Usamos un código de solicitud (en este caso, 1)
    }

    // Aquí agregas el método onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            // Si el resultado es OK, significa que se modificó o eliminó un usuario
            cargarLista()  // Aquí volvemos a cargar la lista actualizada
        }
    }

    private fun cargarLista() {
        // Limpiar la lista para evitar duplicados
        listaUsuario.clear()
        listaFiltrada.clear()

        val url = "http://52.91.135.89/consulta.php" // tu URL AWS
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONArray(response)

                    // Recorrer la respuesta y agregar los usuarios a la lista
                    for (i in 0 until json.length()) {
                        val usuario = json.getJSONObject(i)
                        val id = usuario.getString("id")
                        val nombre = usuario.getString("nombres")
                        val apellido = usuario.getString("apellidos")

                        // Formato: "ID - Nombre Apellido"
                        val linea = "$id - $nombre $apellido"
                        listaUsuario.add(linea)
                    }

                    // Actualizar la lista filtrada y notificar al adaptador
                    listaFiltrada.addAll(listaUsuario)
                    adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaFiltrada)
                    listado.adapter = adapter
                    adapter.notifyDataSetChanged()

                } catch (e: Exception) {
                    Toast.makeText(this, "Error parseando JSON: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        dato.add(request)
    }

    private fun filtrarDatos(query: String) {
        if (!::adapter.isInitialized) return

        listaFiltrada.clear()

        if (query.isEmpty()) {
            // Si no hay texto → restauramos toda la lista
            listaFiltrada.addAll(listaUsuario)
        } else {
            // Si hay texto → filtramos por nombre o apellido
            for (item in listaUsuario) {
                val textoNormalizado = normalizar(item)
                val queryNormalizada = normalizar(query)
                if (textoNormalizado.contains(queryNormalizada)) {
                    listaFiltrada.add(item)
                }
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun normalizar(texto: String): String {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace("[^\\p{ASCII}]".toRegex(), "")
            .lowercase()
    }
}



