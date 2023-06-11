package com.example.objectdetection

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat

class HomePage0 : AppCompatActivity() {
    lateinit var button1: Button
    lateinit var button2: Button
    lateinit var button3: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page0)
        button1 = findViewById(R.id.b1)
        button2 = findViewById(R.id.b2)
        getCameraAccess()
        val intent = Intent(this,HomePage::class.java)
        button1.setOnClickListener{
            startActivity(intent)
        }
        val intent2 = Intent(this, swapCamera::class.java)
        button2.setOnClickListener{
            startActivity(intent2)
        }
        button3 = findViewById(R.id.b3)
        button3.setOnClickListener{
            startActivity(Intent(this, DetectionVideo::class.java))
        }
    }
    fun getCameraAccess(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0]!= PackageManager.PERMISSION_GRANTED){
            getCameraAccess()
        }
    }
}