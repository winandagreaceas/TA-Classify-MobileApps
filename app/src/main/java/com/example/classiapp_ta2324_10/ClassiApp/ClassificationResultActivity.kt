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

    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val imageUriString = intent.getStringExtra("image_uri")
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)

            // Tampilkan gambar pada ImageView
            binding.imageResult.setImageURI(imageUri)

            // Memanggil TFLite Model
            try {
                tflite = Interpreter(loadModelFile())
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }

            // KLASIFIKASI
            val result = classifyImage(imageUri)
            displayResult(result)
        }

        // Set OnClickListener untuk button Kembali Ke Beranda
        binding.btnBeranda.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Set OnClickListener untuk button Tutup Aplikasi
        binding.btnExit.setOnClickListener {
            finishAffinity() // Menutup semua aktivitas dan keluar dari aplikasi
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun classifyImage(uri: Uri): FloatArray {
        // Load bitmap from the URI
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }
        for (x in 0 until 224) {
            for (y in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                input[0][x][y][0] = (pixel shr 16 and 0xFF) / 255.0f
                input[0][x][y][1] = (pixel shr 8 and 0xFF) / 255.0f
                input[0][x][y][2] = (pixel and 0xFF) / 255.0f
            }
        }

        val output = Array(1) { FloatArray(2) } // Assuming model outputs probabilities for 2 classes
        tflite.run(input, output)
        return output[0]
    }

    private fun displayResult(result: FloatArray) {
        val gender = if (result[0] > result[1]) "Male" else "Female"
        val accuracy = maxOf(result[0], result[1]) * 100

        binding.textGender.text = gender
        binding.textHasil.setOnClickListener {
            binding.textPercent.text = "Accuracy: %.2f%%".format(accuracy)
        }
    }
}