package com.daniel.apibusunhevalconductor.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.daniel.apibusunhevalconductor.R
import com.daniel.apibusunhevalconductor.databinding.ActivityProfileBinding
import com.daniel.apibusunhevalconductor.models.Conductor
import com.daniel.apibusunhevalconductor.providers.AuthProvider
import com.daniel.apibusunhevalconductor.providers.ConductorProvider
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val conductorProvider = ConductorProvider()
    val authProvider = AuthProvider()

    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        getConductor()
        binding.imageViewBack.setOnClickListener { finish() }
        binding.btnUpdate.setOnClickListener { updateInfo() }
        binding.circleImageProfile.setOnClickListener { selectImage() }
    }

    private fun updateInfo(){
        val name = binding.textFieldName.text.toString()
        val lastname = binding.textFieldLastName.text.toString()
        val phone = binding.textFieldPhone.text.toString()
        val carRut = binding.textRutCar.text.toString()
        val carColor = binding.textColorCar.text.toString()
        val carPlate = binding.textCarPlate.text.toString()

        val conductor = Conductor(
            id = authProvider.getId(),
            name = name,
            lastname = lastname,
            phone = phone,
            colorcar = carColor,
            rutcar = carRut,
            platenumber = carPlate
        )

        if(imageFile != null){

            conductorProvider.uploadImage(authProvider.getId(), imageFile!!).addOnSuccessListener { taskSnapshot->
                conductorProvider.getImageUrl().addOnSuccessListener { url->

                    val imageUrl = url.toString()
                    conductor.image = imageUrl
                    conductorProvider.update(conductor).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@ProfileActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                        }
                    }
                    Log.d("STORAGE","URL: $imageUrl")

                }
            }

        }else{

            conductorProvider.update(conductor).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@ProfileActivity, "No se pudo actualizar la información", Toast.LENGTH_LONG).show()
                }
            }

        }


    }

    private fun getConductor(){
        conductorProvider.getConductor(authProvider.getId()).addOnSuccessListener { document ->

            if (document.exists()){
                val conductor = document.toObject(Conductor::class.java)
                binding.textViewEmail.text = conductor?.email
                binding.textFieldName.setText(conductor?.name)
                binding.textFieldLastName.setText(conductor?.lastname)
                binding.textFieldPhone.setText(conductor?.phone)
                binding.textFieldName.setText(conductor?.name)
                binding.textRutCar.setText(conductor?.rutcar)
                binding.textColorCar.setText(conductor?.colorcar)
                binding.textCarPlate.setText(conductor?.platenumber)

                if (conductor?.image != null){
                    if(conductor.image != ""){
                        Glide.with(this).load(conductor.image).into(binding.circleImageProfile)
                    }
                }

            }

        }
    }

    private var starImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){

            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.circleImageProfile.setImageURI(fileUri)

        }else if(resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, "Tarea cancelada", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                starImageForResult.launch(intent)
            }
    }

}