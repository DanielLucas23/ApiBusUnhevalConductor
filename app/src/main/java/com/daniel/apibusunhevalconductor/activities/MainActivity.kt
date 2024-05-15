package com.daniel.apibusunhevalconductor.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.daniel.apibusunhevalconductor.databinding.ActivityMainBinding
import com.daniel.apibusunhevalconductor.providers.AuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val authProvider = AuthProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        //Click al Botton
        binding.btnRegister.setOnClickListener{ goToRegister() }
        //
        binding.btnLogin.setOnClickListener{ Login() }
    }

    private fun Login(){
        //Obtener los datos ingresados de los inputs
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()

        if(isValidForm(email,password)){
            authProvider.login(email, password).addOnCompleteListener {
                if(it.isSuccessful){
                    goToMap()
                }else{
                    Toast.makeText(this@MainActivity, "Error al iniciar sesion", Toast.LENGTH_SHORT).show()
                    Log.d("FIREBASE", "ERROR: + ${it.exception.toString()}")
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

    private fun isValidForm(email:String, password:String):Boolean{
        if (email.isEmpty()){
            Toast.makeText(this, "Ingresa tu correo electronico", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()){
            Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    //Para cambiar de pantalla
    private fun goToRegister(){
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    //ciclo de vida
    override fun onStart() {
        super.onStart()

        if(authProvider.existSession()){
            goToMap()
        }
    }


}