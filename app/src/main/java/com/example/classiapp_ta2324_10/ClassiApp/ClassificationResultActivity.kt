package com.example.classiapp_ta2324_10.ClassiApp
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.example.classiapp_ta2324_10.R
import com.example.classiapp_ta2324_10.databinding.ActivityClassificationResultBinding
import com.example.classiapp_ta2324_10.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ClassificationResultActivity : AppCompatActivity() {
    val binding: ActivityClassificationResultBinding by lazy {
        ActivityClassificationResultBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val imageUriString = intent.getStringExtra("image_uri")
        val imageUri = Uri.parse(imageUriString)

        // Tampilkan gambar pada ImageView
        binding.imageResult.setImageURI(imageUri)

        // Set OnClickListener untuk tombol btn_beranda
        binding.btnBeranda.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Set OnClickListener untuk tombol btn_exit
        binding.btnExit.setOnClickListener {
            finishAffinity() // Menutup semua aktivitas dan keluar dari aplikasi
        }
    }
}