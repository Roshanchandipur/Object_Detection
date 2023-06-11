package com.example.objectdetection

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignUp : AppCompatActivity() {
    lateinit var firebase: FirebaseAuth
    var eye1 = false
    var eye2 = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        firebase = FirebaseAuth.getInstance()
        val p1 = findViewById<EditText>(R.id.inputPassword)
        val t1 = findViewById<TextView>(R.id.text1)


        p1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0!=null){

                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0 != null) {
                    var b1 = false
                    var b2 = false
                    var b3 = false
                    var b4 = false
                    for (i in p1 until p1 + p3) {
                        if (p0[i] >= 'a' && p0[i] <= 'z') {
                            b2 = true
                        }
                        if (p0[i] >= 'A' && p0[i] <= 'Z') {
                            b1= true
                        }
                        if (p0[i] >= '0' && p0[i] <= '9')
                            b3 = true
                        if (p0[i] == '@' || p0[i] == '!')
                            b4 =true
                    }
                    if(b1 and b2 and b3 and b4){
                        t1.visibility = View.GONE
                    }
                    else
                        t1.visibility = View.VISIBLE
                }


            }

            override fun afterTextChanged(p0: Editable?) {

                if (p0 != null) {
                    val set = p0.toString().toSet()

                    var b1 = false
                    var b2 = false
                    var b3 = false
                    var b4 = false
                    for (ch in set) {
                        if (ch >= 'a' && ch <= 'z') b1 = true
                        if (ch >= 'A' && ch <= 'Z') b2 = true
                        if (ch == '@' || ch == '!') b3 = true
                        if (ch >= '0' && ch <= '9') b4 = true
                    }
                    if(b1 and b2 and b3 and b4){
                        t1.visibility = View.GONE
                    }
                    else
                        t1.visibility = View.VISIBLE
                }
            }

        })
    }
    fun hidden(view: View) {
        eye1 = !eye1
        val e1 = findViewById<EditText>(R.id.inputPassword)
        val cursor = e1.selectionStart
        if (eye1) {
            e1.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            e1.setSelection(cursor)
        } else {
            e1.transformationMethod = null
            e1.setSelection(cursor)
        }

    }


    fun hide(view: View) {
        eye2 = !eye2
        val e2 = findViewById<EditText>(R.id.inputPasswordC)
        val cursor = e2.selectionStart
        if (eye2) {
            e2.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            e2.setSelection(cursor)
        } else {
            e2.transformationMethod = null
            e2.setSelection(cursor)
        }
    }

    fun signUp1(view: View) {
        val user = findViewById<EditText>(R.id.inputEmail).text.toString()
        val password = findViewById<EditText>(R.id.inputPassword).text.toString()
        val cPassword = findViewById<EditText>(R.id.inputPasswordC).text.toString()
        val name = findViewById<EditText>(com.example.objectdetection.R.id.name).text.toString()

        if (user.isEmpty() && password.isEmpty()) {
            Toast.makeText(
                this,
                "you must choose user name and password",
                Toast.LENGTH_LONG
            ).show()
        }
        else if (name.isEmpty())
            Toast.makeText(this, "name can't be empty", Toast.LENGTH_SHORT).show()
        else if (user.isEmpty()) {
            Toast.makeText(this, "user name can't be blank", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Password can't be empty", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(this, "Password and confirm password are not same", Toast.LENGTH_LONG)
                .show()
        } else {
            val loader = findViewById<ProgressBar>(R.id.load)

            loader.visibility = View.VISIBLE
            firebase.createUserWithEmailAndPassword(user, password).addOnCompleteListener(this) {
                loader.visibility = View.GONE
                if (it.isSuccessful) {
                    Toast.makeText(this, "Your account has been created", Toast.LENGTH_LONG).show()
                    val user = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    val f  = FirebaseAuth.getInstance().currentUser
                    f?.updateProfile(user)?.addOnCompleteListener{
                        Toast.makeText(this, "name updated", Toast.LENGTH_SHORT).show()
                    }
                    if(f!=null){
                        val user = User(f.uid, name, f.photoUrl.toString())
                        DAO().add(user, this)
                    }
                    else
                        Toast.makeText(this, "f is null", Toast.LENGTH_LONG).show()

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Something went wrong try again", Toast.LENGTH_LONG).show()
                }
            }

        }
    }
}