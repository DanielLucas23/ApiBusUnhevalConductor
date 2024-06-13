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
        var IDD: String = txt_id.text.toString()

        tuCollection.document(IDD)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this,"Eliminado correctamente", Toast.LENGTH_SHORT).show()
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

        var pre:String = txt_pregunta.text.toString()
        var res:String = txt_respuesta.text.toString()
        var IDD: String = txt_id.text.toString()
        val docActualizado = HashMap<String, Any>()
        docActualizado["pregunta"]=pre
        docActualizado["respuesta"]=res
        tuCollection.document(IDD)
            .update(docActualizado)
            .addOnSuccessListener {
                Toast.makeText(this,"Actualización exitosa", Toast.LENGTH_SHORT).show()
                consultarColeccion()
            }
            .addOnFailureListener {e->
                Toast.makeText(this,"Error: " + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun insertarColeccion() {
        val db = FirebaseFirestore.getInstance()
        val txt_pregunta: TextView = findViewById(R.id.txt_Pregunta)
        val txt_respuesta: TextView = findViewById(R.id.txt_Respuesta)
        var pre:String = txt_pregunta.text.toString()
        var res:String = txt_respuesta.text.toString()

        if (pre != "" && res != ""){

            val data = hashMapOf(
                "pregunta" to pre,
                "respuesta" to res
            )
            db.collection("Preguntas")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this,"Registro exitoso", Toast.LENGTH_SHORT).show()
                    consultarColeccion()
                }
                .addOnFailureListener { e -> }
        }else{
            Toast.makeText(this,"Rellene los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun consultarColeccion(){
        tuCollection.get()
            .addOnSuccessListener {
                val listaTuModelo = mutableListOf<Preguntas>()
                for (document in it){
                    val pregunta = document.getString("pregunta")
                    val respuesta = document.getString("respuesta")
                    val ID = document.id
                    if(pregunta != null && respuesta != null){
                        val tuModelo = Preguntas(ID,pregunta,respuesta)
                        listaTuModelo.add((tuModelo))
                    }
                }

                adapter.setDatos(listaTuModelo)

            }
    }

    override fun onItemClick(tuModelo: Preguntas) {
        val txt_pregunta: TextView = findViewById(R.id.txt_Pregunta)
        val txt_respuesta: TextView = findViewById(R.id.txt_Respuesta)
        val txt_id: TextView = findViewById(R.id.txt_ID)

        txt_pregunta.text=tuModelo.pregunta
        txt_respuesta.text=tuModelo.respuesta
        txt_id.text=tuModelo.id
    }
}