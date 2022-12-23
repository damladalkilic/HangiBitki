package com.example.HangiBitki

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.HangiBitki.databinding.ActivityMainBinding
import com.example.HangiBitki.ml.FlowerModel
import com.example.HangiBitki.R
import org.tensorflow.lite.support.image.TensorImage
import java.io.IOException
import java.nio.charset.Charset
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var tvOutput: TextView
    private val GALLERY_REQUEST_CODE = 123
    val idName = "BITKI_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bakimButton = findViewById<Button>(R.id.bakim_btn)
        bakimButton.setOnClickListener {
            val intent = Intent(this, NextActivity::class.java)
            intent.putExtra(idName, tvOutput.text)
            startActivity(intent)
        }

        imageView = binding.imageView
        button = binding.btnCaptureImage
        tvOutput = binding.tvOutput
        val buttonLoad = binding.btnLoadImage

        button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                takePicturePreview.launch(null)
            } else {
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
        buttonLoad.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "iamge/png", "image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                onresult.launch(intent)
            } else {
                requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }


        //to download image when longPress on ImageView
        imageView.setOnLongClickListener {
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return@setOnLongClickListener true
        }


    }

    //request camera permission
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            takePicturePreview.launch(null)
        } else {
            Toast.makeText(this, "İzin verilmedi! Tekrar deneyin", Toast.LENGTH_SHORT).show()
        }
    }

    //launch camera and take picture
    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
            outputGenerator(bitmap)
        }
    }

    //to get image from gallery
    private val onresult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.i("TAG", "This is the result: ${result.data} ${result.resultCode}")
        onResultReceived(GALLERY_REQUEST_CODE, result)
    }

    private fun onResultReceived(requestCode: Int, result: androidx.activity.result.ActivityResult?) {
        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (result?.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        Log.i("TAG", "onResultReceived: $uri")
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        imageView.setImageBitmap(bitmap)
                        outputGenerator(bitmap)
                    }
                } else {
                    Log.e("TAG", "onActivityResult: error in selecting image")
                }
            }
        }
    }

    private fun outputGenerator(bitmap: Bitmap) {
        //declearing tensorflow lite model variable
        val flowerModel = FlowerModel.newInstance(this)

        // converting bitmap into tensorflow lite image
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfimage = TensorImage.fromBitmap(newBitmap)

        // process the image using trained model and sort it in descending order
        val outputs = flowerModel.process(tfimage)
            .probabilityAsCategoryList.apply {
                sortByDescending { it.score }
            }

        //getting result having high probability
        val highProbabilityOutput = outputs[0]
        val flowerId = highProbabilityOutput.label
        var flowerText = ""
        try {
            val obj = JSONObject(getJSONFromAssets()!!)
            val usersArray = obj.getJSONArray("bitkiler")

            for (i in 0 until usersArray.length()) {

                val user = usersArray.getJSONObject(i)

                val id = user.getString("id")
                val bitkiJsonId = user.getString("id")
                val bitkiAdi = user.getString("bitkiAdi")
                if (bitkiJsonId == flowerId) {
                    flowerText = bitkiAdi
                }
            }
        } catch (e: JSONException) {
            //exception
            e.printStackTrace()
        }


        //setting output text
        tvOutput.text = flowerText
        Log.i("TAG", "outputGenerator: $highProbabilityOutput")

    }

    //to download image to device
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                AlertDialog.Builder(this).setTitle("Fotoğrafı indir?")
                    .setMessage("Bu fotoğrafı indirmek istiyor musunuz?")
                    .setPositiveButton("Yes") { _, _ ->
                        val drawable: BitmapDrawable = imageView.drawable as BitmapDrawable
                        val bitmap = drawable.bitmap
                        downloadImage(bitmap)
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                Toast.makeText(this, "Lütfen fotoğrafı indirmek için izin verin", Toast.LENGTH_LONG).show()
            }
        }

    //fun that takes a bitmap and store to user's device
    private fun downloadImage(mBitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Plant_Images" + System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        if (uri != null) {
            contentResolver.insert(uri, contentValues)?.also {
                contentResolver.openOutputStream(it).use { outputStream ->
                    if (!mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                        throw IOException("Bitmap kaydedilemedi")
                    } else {
                        Toast.makeText(applicationContext, "Fotoğraf kaydedildi", Toast.LENGTH_LONG).show()
                    }
                }
                return it
            }
        }
        return null
    }

    fun getJSONFromAssets(): String? {

        var json: String? = null
        val charset: Charset = Charsets.UTF_8
        try {
            val myUsersJSONFile = assets.open("Users.json")
            val size = myUsersJSONFile.available()
            val buffer = ByteArray(size)
            myUsersJSONFile.read(buffer)
            myUsersJSONFile.close()
            json = String(buffer, charset)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

}
