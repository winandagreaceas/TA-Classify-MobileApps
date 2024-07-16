package com.example.classiapp_ta2324_10.ClassiApp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.classiapp_ta2324_10.R

class ClassificationResultActivity : AppCompatActivity() {
    lateinit var btnBeranda: Button
    lateinit var btnExit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classification_result)

        val classificationResult = intent.getStringExtra("textHasil") ?: "Data tidak tersedia"
        val imageUri = intent.getStringExtra("imageUri")

        // Tampilkan gambar pada ImageView
        val imageResult = findViewById<ImageView>(R.id.imagePreview)
        imageResult.setImageURI(Uri.parse(imageUri))

        findViewById<TextView>(R.id.textHasil).text = classificationResult

        btnBeranda = findViewById(R.id.btn_beranda)
        btnExit = findViewById(R.id.btn_exit)

        // Set OnClickListener untuk tombol btn_beranda
        btnBeranda.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Set OnClickListener untuk tombol btn_exit
        btnExit.setOnClickListener {
            finishAffinity() // Menutup semua aktivitas dan keluar dari aplikasi
        }
    }
}