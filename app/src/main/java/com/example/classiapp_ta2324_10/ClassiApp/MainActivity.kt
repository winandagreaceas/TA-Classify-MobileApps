package com.example.classiapp_ta2324_10.ClassiApp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.classiapp_ta2324_10.R
import com.example.classiapp_ta2324_10.databinding.ActivityMainBinding
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imagePreview: ImageView
    private lateinit var imageUri: Uri // Tambahkan variabel untuk menyimpan URI gambar

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val pickImageButton = findViewById<Button>(R.id.btn_pick_image)
        val classifyButton = findViewById<Button>(R.id.btn_klasifikasi)
        imagePreview = findViewById(R.id.imagePreview)

        pickImageButton.setOnClickListener {
            openGallery()
        }

        classifyButton.isEnabled = false // Disable classify button initially

        toDetail(classifyButton) // Panggil metode toDetail() di sini untuk menangani klik tombol btn Klasifikasi
    }

    private fun toDetail(classifyButton: Button) {
        classifyButton.setOnClickListener {
            if (::imageUri.isInitialized) {
                val intent = Intent(this, ClassificationResultActivity::class.java)
                intent.putExtra("image_uri", imageUri.toString()) // Kirim URI gambar sebagai string
                startActivity(intent)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Pilih Foto"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!! // Simpan URI gambar yang dipilih
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                val rotatedBitmap = rotateImageIfRequired(bitmap, imageUri)
                imagePreview.setImageBitmap(rotatedBitmap)
                binding.btnKlasifikasi.isEnabled = true // Enable classify button after image is selected
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, selectedImage: Uri): Bitmap {
        val ei: ExifInterface
        try {
            ei = ExifInterface(selectedImage.path!!)
        } catch (ex: IOException) {
            return bitmap
        }

        val orientation: Int = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
