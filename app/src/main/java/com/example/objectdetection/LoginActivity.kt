package com.example.objectdetection

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var googleSignInButton: View
    private lateinit var firebaseAuth: FirebaseAuth
    private var hidden = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val textView = findViewById<TextView>(R.id.text4)
        textView.paintFlags = (textView.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG)
        googleSignInButton = findViewById<SignInButton>(R.id.signInButton)
        firebaseAuth = FirebaseAuth.getInstance()
        googleSignInButton.setOnClickListener {
            Toast.makeText(this, "Under maintenance", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val user = firebaseAuth.currentUser
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun hidePassword(view: View) {
        val inputPassword = findViewById<EditText>(R.id.inputPassword)
        hidden = !hidden
        val cursor = inputPassword.selectionStart
        if (hidden) {
            inputPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            inputPassword.setSelection(cursor)
        } else {
            inputPassword.transformationMethod = null
            inputPassword.setSelection(cursor)
        }
    }

    fun signUp(view: View) {
        val intent = Intent(this, SignUp::class.java)
        startActivity(intent)
        finish()
    }

    fun signIn(view: View) {
        val user = findViewById<EditText>(R.id.inputEmail).text.toString()
        val password = findViewById<EditText>(R.id.inputPassword).text.toString()
        if (user.isEmpty() && password.isEmpty()) {
            Toast.makeText(
                this,
                "you must provide correct user name and password",
                Toast.LENGTH_LONG
            ).show()
        } else if (user.isEmpty()) {
            Toast.makeText(this, "user name can't be blank", Toast.LENGTH_LONG).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Password can't be empty", Toast.LENGTH_LONG).show()
        }
        else{
            displayProgressBar()
            firebaseAuth.signInWithEmailAndPassword(user, password).addOnCompleteListener{

                if(it.isSuccessful){
                    displayProgressBar()
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                else{
                    Toast.makeText(this, "Login failed check user_name and password" , Toast.LENGTH_LONG).show()
                    displayProgressBar()
                }

            }
        }
    }

    private fun displayProgressBar() {
        val progressBar = findViewById<ProgressBar>(R.id.load)
        if (progressBar.isVisible)
            progressBar.visibility = View.GONE
        else
            progressBar.visibility = View.VISIBLE
    }
}