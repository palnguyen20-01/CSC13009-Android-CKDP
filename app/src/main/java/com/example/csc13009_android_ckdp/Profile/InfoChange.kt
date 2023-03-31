package com.example.csc13009_android_ckdp.Profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.csc13009_android_ckdp.R
import com.example.csc13009_android_ckdp.SettingFragment
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.util.*

class InfoChange : AppCompatActivity() {
    lateinit var btnCancel : Button
    lateinit var btnSave : Button
    var encodedImage : String = ""
    lateinit var imageProfile : ImageView
    @RequiresApi(Build.VERSION_CODES.O)
    public
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changeinfo)

        btnCancel = findViewById(R.id.btnCancelChangeProfile)
        btnSave = findViewById(R.id.btnSaveChangeProfile)
        imageProfile = findViewById(R.id.imageChangeProfile)

        imageProfile.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)

        }

        btnSave.setOnClickListener {
            val intent = Intent(this, PasswordChange::class.java)
            intent.putExtra("image", encodedImage)
            setResult(1, intent);
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun encodeImage(bitmap : Bitmap) : String{
        var previewWidth = 150
        var previewHeight = bitmap.height * previewWidth / bitmap.width
        var previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        var byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        var bytes = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(bytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if(result.data != null){
                var imageUri = result.data!!.data!!
                try{
                    var inputStream = contentResolver.openInputStream(imageUri)
                    var bitmap = BitmapFactory.decodeStream(inputStream)
                    imageProfile.setImageBitmap(bitmap)
                    encodedImage = encodeImage(bitmap)

                }catch(e : FileNotFoundException){
                    e.printStackTrace()
                }
            }
        }
    }

}