package com.daniel.apibusunhevalconductor.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daniel.apibusunhevalconductor.R
import com.daniel.apibusunhevalconductor.adapters.PreguntasAdapter
import com.daniel.apibusunhevalconductor.models.Preguntas
import com.google.firebase.firestore.FirebaseFirestore

class PreguntasActivity : AppCompatActivity(), PreguntasAdapter.OnItemClickListener {

    private val db = FirebaseFirestore.getInstance()
    private val tuCollection = db.collection("Preguntas")
    private lateinit var recycleView: RecyclerView
    private lateinit var adapter: PreguntasAdapter
    private val listaTuModelo = mutableListOf<Preguntas>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preguntas)

        recycleView = findViewById(R.id.rDatos)
        recycleView.layoutManager = LinearLayoutManager(this)
        adapter = PreguntasAdapter(this)
        recycleView.adapter = adapter

        val btnConsultar: Button = findViewById(R.id.btn_Consultar)
        val btnInsertar: Button = findViewById(R.id.btn_Insertar)
        val btnActualizar: Button = findViewById(R.id.btnActualizar)
        val btnEliminar: Button = findViewById(R.id.btnEliminar)

        btnEliminar.setOnClickListener {
            eliminarColeccion()
        }

        btnActualizar.setOnClickListener {
            actualizarColeccion()
        }

        btnConsultar.setOnClickListener {
            consultarColeccion()
        }

        btnInsertar.setOnClickListener {
            insertarColeccion()
        }
    }

    private fun eliminarColeccion() {
        val txt_id: TextView = findViewById(R.id.txt_ID)
        val txt_pregunta: TextView = findViewById(R.id.txt_Pregunta)
        val txt_respuesta: TextView = findViewById(R.id.txt_Respuesta)
        val IDD: String = txt_id.text.toString()

        tuCollection.document(IDD)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this,"Eliminado correctamente", Toast.LENGTH_SHORT).show()
                // Limpiar campos
                txt_id.text = "ID"
                txt_pregunta.text = ""
                txt_respuesta.text = ""
                // Mover el foco a la primera caja de texto
                txt_pregunta.requestFocus()
                consultarColeccion()
            }
            .addOnFailureListener { e->
                Toast.makeText(this,"Error: " + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarColeccion() {
        val txt_pregunta: TextView = findViewById(R.id.txt_Pregunta)
        val txt_respuesta: TextView = findViewById(R.id.txt_Respuesta)
        val txt_id: TextView = findViewById(R.id.txt_ID)

        val pre = txt_pregunta.text.toString()
        val res = txt_respuesta.text.toString()
        val IDD: String = txt_id.text.toString()
        val docActualizado = HashMap<String, Any>()
        docActualizado["pregunta"] = pre
        docActualizado["respuesta"] = res
        tuCollection.document(IDD)
            .update(docActualizado)
            .addOnSuccessListener {
                Toast.makeText(this,"Actualización exitosa", Toast.LENGTH_SHORT).show()
                // Limpiar campos
                txt_id.text = "ID"
                txt_pregunta.text = ""
                txt_respuesta.text = ""
                // Mover el foco a la primera caja de texto
                txt_pregunta.requestFocus()
                consultarColeccion()
            }
            .addOnFailureListener { e->
                Toast.makeText(this,"Error: " + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun insertarColeccion() {
        val txt_pregunta: TextView = findViewById(R.id.txt_Pregunta)
        val txt_respuesta: TextView = findViewById(R.id.txt_Respuesta)
        val pre = txt_pregunta.text.toString()
        val res = txt_respuesta.text.toString()

        if (pre.isNotEmpty() && res.isNotEmpty()) {
            val data = hashMapOf(
                "pregunta" to pre,
                "respuesta" to res
            )
            db.collection("Preguntas")
                .add(data)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    // Limpiar los campos después del registro exitoso
                    txt_pregunta.text = ""
                    txt_respuesta.text = ""
                    // Mover el foco a la primera caja de texto
                    txt_pregunta.requestFocus()
                    // Agregar el nuevo item a la lista y notificar al adapter
                    listaTuModelo.add(Preguntas(documentReference.id, pre, res))
                    adapter.setDatos(listaTuModelo)
                    // Desplazar el RecyclerView al final
                    recycleView.scrollToPosition(listaTuModelo.size - 1)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this,"Error: " + e.message, Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this,"Rellene los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun consultarColeccion() {
        tuCollection.get()
            .addOnSuccessListener { result ->
                listaTuModelo.clear()
                for (document in result) {
                    val pregunta = document.getString("pregunta")
                    val respuesta = document.getString("respuesta")
                    val ID = document.id
                    if (pregunta != null && respuesta != null) {
                        val tuModelo = Preguntas(ID, pregunta, respuesta)
                        listaTuModelo.add(tuModelo)
                    }
                }
                adapter.setDatos(listaTuModelo)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,"Error: " + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onItemClick(tuModelo: Preguntas) {
        val txt_pregunta: TextView = findViewById(R.id.txt_Pregunta)
        val txt_respuesta: TextView = findViewById(R.id.txt_Respuesta)
        val txt_id: TextView = findViewById(R.id.txt_ID)

        txt_pregunta.text = tuModelo.pregunta
        txt_respuesta.text = tuModelo.respuesta
        txt_id.text = tuModelo.id
    }
}