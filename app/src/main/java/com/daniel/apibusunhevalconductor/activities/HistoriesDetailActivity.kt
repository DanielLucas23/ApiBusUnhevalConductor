package com.daniel.apibusunhevalconductor.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.daniel.apibusunhevalconductor.databinding.ActivityHistoryDetailBinding
import com.daniel.apibusunhevalconductor.models.Estudiante
import com.daniel.apibusunhevalconductor.models.History
import com.daniel.apibusunhevalconductor.providers.EstudianteProvider
import com.daniel.apibusunhevalconductor.providers.HistoryProvider
import com.daniel.apibusunhevalconductor.utils.RelativeTime

class HistoriesDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding
    private var historyProvider = HistoryProvider()
    private var estudianteProvider = EstudianteProvider()
    private var extraId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        extraId = intent.getStringExtra("id")!!
        getHistory()

        binding.imageViewBack.setOnClickListener { finish() }
    }

    private fun getHistory (){
        historyProvider.getHistoryById(extraId).addOnSuccessListener { document->

            if (document.exists()){
                val history = document.toObject(History::class.java)
                binding.textViewOrigin.text = history?.origin
                binding.textViewDestination.text = history?.destination
                binding.textViewDate.text = RelativeTime.getTimeAgo(history?.timestamp!!, this@HistoriesDetailActivity)
                binding.textViewMyCalification.text = "${history?.calificationToConductor}"
                binding.textViewEstudianteCalification.text = "${history?.calificationToEstudiante}"
                binding.textViewTimeAndDistance.text = "${history?.time} Min - ${String.format("%.1f", history?.km)} Km"

                getEstudianteInfo(history?.idEstudiante!!)
            }

        }
    }

    private fun getEstudianteInfo(id: String){
        estudianteProvider.getClienById(id).addOnSuccessListener { document ->
            if (document.exists()){
                val estudiante = document.toObject(Estudiante::class.java)
                binding.textViewEmail.text = estudiante?.email
                binding.textViewName.text = "${estudiante?.name} ${estudiante?.lastname}"

                if (estudiante?.image != null){
                    if (estudiante?.image != ""){
                        Glide.with(this).load(estudiante?.image).into(binding.circleImageProfile)
                    }
                }
            }
        }
    }

}