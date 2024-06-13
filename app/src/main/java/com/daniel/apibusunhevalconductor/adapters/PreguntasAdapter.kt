package com.daniel.apibusunhevalconductor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daniel.apibusunhevalconductor.R
import com.daniel.apibusunhevalconductor.models.Preguntas

class PreguntasAdapter(private val itemClickListener: OnItemClickListener): RecyclerView.Adapter<PreguntasAdapter.ViewHolder> (){

    private var datos: List<Preguntas> = ArrayList()

    interface OnItemClickListener {
        fun onItemClick(tuModelo: Preguntas)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtID: TextView = itemView.findViewById(R.id.txtID)
        val txtPregunta: TextView = itemView.findViewById(R.id.txtPregunta)
        val txtRespuesta: TextView = itemView.findViewById(R.id.txtRespuesta)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position!= RecyclerView.NO_POSITION){
                    val tuModelo = datos[position]
                    itemClickListener.onItemClick(tuModelo)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pregunta_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        //Tamaño de la colección
        return datos.size
    }

    fun setDatos(datos: List<Preguntas>){
        this.datos = datos
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datos[position]

        holder.txtID.text = item.id.toString()
        holder.txtPregunta.text = item.pregunta.toString()
        holder.txtRespuesta.text = item.respuesta.toString()
    }

}