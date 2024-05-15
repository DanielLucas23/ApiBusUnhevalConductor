package com.daniel.apibusunhevalconductor.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.daniel.apibusunhevalconductor.R
import com.daniel.apibusunhevalconductor.activities.HistoriesDetailActivity
import com.daniel.apibusunhevalconductor.models.History
import com.daniel.apibusunhevalconductor.utils.RelativeTime

class HistoriesAdapter(val context: Activity, val histories: ArrayList<History>): RecyclerView.Adapter<HistoriesAdapter.HistoriesAdapterViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriesAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_history, parent, false)
        return HistoriesAdapterViewHolder(view)
    }

    //Establecer la info
    override fun onBindViewHolder(holder: HistoriesAdapterViewHolder, position: Int) {

        val history = histories[position] //Un solo historial
        holder.textViewOrigin.text = history.origin
        holder.textViewDestination.text = history.destination

        if (history.timestamp != null){
            holder.textViewDate.text = RelativeTime.getTimeAgo(history.timestamp!!, context)
        }

            holder.itemView.setOnClickListener { gotoDetail(history?.id!!) }

    }

    private fun gotoDetail(idHistory: String){
        val i = Intent(context, HistoriesDetailActivity::class.java)
        i.putExtra("id", idHistory)
        context.startActivity(i)
    }

    //Tamaño de la lista
    override fun getItemCount(): Int {
        return histories.size
    }




    class HistoriesAdapterViewHolder(view: View): RecyclerView.ViewHolder(view){
        val textViewOrigin: TextView
        val textViewDestination: TextView
        val textViewDate: TextView

        init {
            textViewOrigin = view.findViewById(R.id.textViewOrigin)
            textViewDestination = view.findViewById(R.id.textViewDestination)
            textViewDate = view.findViewById(R.id.textViewDate)
        }
    }


}