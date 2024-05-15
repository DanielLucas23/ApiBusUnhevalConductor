package com.daniel.apibusunhevalconductor.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.daniel.apibusunhevalconductor.R
import com.daniel.apibusunhevalconductor.adapters.HistoriesAdapter
import com.daniel.apibusunhevalconductor.databinding.ActivityHistoriesBinding
import com.daniel.apibusunhevalconductor.models.History
import com.daniel.apibusunhevalconductor.providers.HistoryProvider

class HistoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoriesBinding

    private var historyProvider = HistoryProvider()
    private var histories = ArrayList<History>()

    private lateinit var adapter: HistoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager = LinearLayoutManager(this)
        binding.recycleViewHistories.layoutManager = linearLayoutManager

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Historial de Viajes"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getHistories()
    }

    private fun getHistories(){
        histories.clear()

        historyProvider.getHistories().get().addOnSuccessListener { query ->
            if (query != null){
                if (query.documents.size > 0){
                    val documents = query.documents

                    for (d in documents){
                        var history = d.toObject(History::class.java)
                        history?.id = d.id
                        histories.add(history!!)
                    }

                    adapter = HistoriesAdapter(this@HistoriesActivity, histories)
                    binding.recycleViewHistories.adapter = adapter
                }
            }
        }
    }
}