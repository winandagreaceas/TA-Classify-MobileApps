package com.example.classiapp_ta2324_10.ClassiApp

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.classiapp_ta2324_10.R
import com.example.classiapp_ta2324_10.ml.Model11
import com.example.classiapp_ta2324_10.ml.Model12
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    lateinit var imagePreview: ImageView
    lateinit var pickBtn: Button
    lateinit var classifyBtn: Button
    var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imagePreview = findViewById(R.id.imagePreview)
        pickBtn = findViewById(R.id.btn_pick_image)
        classifyBtn = findViewById(R.id.btn_klasifikasi)

        // Membaca Label Setiap Gambar "label.txt"
        // var labels = application.assets.open("labels.txt").bufferedReader().readLines()
        val labels = arrayListOf<String>("LAKI-LAKI", "PEREMPUAN")
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        // Fungsi button "Unggah Gambar *pick-button*"
        pickBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, 100)
        }

        // Fungsi button "Klasifikasi *classification-button*"
        classifyBtn.setOnClickListener {
            bitmap?.let { bmp ->
                val imageFile = File(cacheDir, "tempImage.jpg")
                val fos = FileOutputStream(imageFile)
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()

                val tempUri = Uri.fromFile(imageFile)

                var tensorImage = TensorImage(DataType.FLOAT32)
                tensorImage.load(bmp)
                tensorImage = imageProcessor.process(tensorImage)

//                // Create and populate ByteBuffer for inputFeature1
//                val byteBuffer = ByteBuffer.allocateDirect(4 * 1 * 1288)
//                byteBuffer.order(ByteOrder.nativeOrder())
//                // Example: Populate ByteBuffer with dummy data
//                for (i in 0 until 1288) {
//                    byteBuffer.putFloat(0.0f)
//                }
//                byteBuffer.rewind()

                val model = Model12.newInstance(this)

                val inputFeature0 =
                    TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(tensorImage.buffer)

//                val inputFeature1 =
//                    TensorBuffer.createFixedSize(intArrayOf(1, 1288), DataType.FLOAT32)
//                inputFeature1.loadBuffer(byteBuffer)

                // Panggil metode process dengan input tambahan
//                val outputs = model.process(inputFeature0, inputFeature1)
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

                var maxIdx = 0
                outputFeature0.forEachIndexed { index, fl ->
                    if (outputFeature0[maxIdx] < fl) {
                        maxIdx = index
                    }
                }
                model.close()

                val gender = labels[maxIdx]

                val intent = Intent(this, ClassificationResultActivity::class.java).apply {
                    putExtra("textHasil", gender)
                    putExtra("imageUri", tempUri.toString())
                }
                startActivity(intent)
            } ?: Toast.makeText(this, "Pilih gambar terlebih dahulu!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null){
            val uri = data.data
            Log.d("dddd", uri.toString())
            bitmap = getBitmap(uri!!)
            Log.d("dddd", bitmap.toString())
//            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            bitmap = rotateImageIfRequired(bitmap!!, uri)
            imagePreview.setImageBitmap(bitmap)
        }
    }

    fun getBitmap(file: Uri): Bitmap?{
        var bitmap1: Bitmap ?= null
        try {
            val inputStream = contentResolver.openInputStream(file)
            bitmap1 = BitmapFactory.decodeStream(inputStream)
            // close stream
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }
        return bitmap1
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, selectedImage: Uri): Bitmap {
        val ei: ExifInterface
        try {
            ei = ExifInterface(selectedImage.path!!)
        } catch (ex: IOException) {
            return bitmap
        }
        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
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