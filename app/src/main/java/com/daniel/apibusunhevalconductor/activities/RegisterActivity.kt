package com.daniel.apibusunhevalconductor.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.daniel.apibusunhevalconductor.databinding.ActivityRegisterBinding
import com.daniel.apibusunhevalconductor.models.Conductor
import com.daniel.apibusunhevalconductor.models.Estudiante
import com.daniel.apibusunhevalconductor.providers.AuthProvider
import com.daniel.apibusunhevalconductor.providers.ConductorProvider
import com.daniel.apibusunhevalconductor.providers.EstudianteProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authProvider = AuthProvider()
    private val conductorProvider = ConductorProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        //Click al Botton
        binding.btnGoToLogin.setOnClickListener{
            GoToLogin()
        }

        //Click en Registrar
        binding.btnRegister.setOnClickListener {
            Register()
        }
    }


    private fun Register(){
        //Obtener los datos ingresados de los inputs
        val name = binding.textFieldName.text.toString()
        val lastname = binding.textFieldLastName.text.toString()
        val phone = binding.textFieldPhone.text.toString()
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()
        val confirmpasswor =binding.textFieldConfirmPassword.text.toString()

        if (isValidForm(name, lastname, phone, email, password, confirmpasswor)){
            authProvider.register(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    val conductor = Conductor(
                        id = authProvider.getId(),
                        name = name,
                        lastname = lastname,
                        phone = phone,
                        email = email
                    )

                    conductorProvider.create(conductor).addOnCompleteListener{
                        if (it.isSuccessful){
                            Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            goToMap()
                        }else{
                            Toast.makeText(this@RegisterActivity, "Hubo un error almacenando los datos del usuario: ${it.exception.toString()}", Toast.LENGTH_SHORT).show()
                            Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                        }
                    }

                }else{
                    Toast.makeText(this@RegisterActivity, "Registro fallido: ${it.exception.toString()}", Toast.LENGTH_LONG).show()
                    Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                }
            }
        }
    }

    //Enviar a la pantalla del mapa

    fun goToMap(){
        val i = Intent(this,MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)

    }

    //Validar Formulario

    private fun isValidForm(name:String, lastname:String, phone:String, email:String, password:String,
                            confirmpassword:String):Boolean{
        if (name.isEmpty()){
            Toast.makeText(this, "Ingresa tu nombre", Toast.LENGTH_SHORT).show()
            return false
        }
        if (lastname.isEmpty()){
            Toast.makeText(this, "Ingresa tus apellidos", Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.isEmpty()){
            Toast.makeText(this, "Ingresa tu número de telefono", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty()){
            Toast.makeText(this, "Ingresa tu correo electronico", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()){
            Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        if (confirmpassword.isEmpty()){
            Toast.makeText(this, "Ingresa tu confirmación de contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmpassword){
            Toast.makeText(this, "Las contraseñas deben coincidir", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6){
            Toast.makeText(this, "La contraseña debe contener al menos 6 caracteres", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    //Para cambiar de pantalla
    private fun GoToLogin(){
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}