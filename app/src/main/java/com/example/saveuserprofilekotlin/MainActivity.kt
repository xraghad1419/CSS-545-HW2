package com.example.saveuserprofilekotlin

import android.Manifest

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import java.io.File

//import com.github.yalantis.ucrop.UCrop
import android.app.Dialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.saveuserprofilekotlin.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri :Uri
    private lateinit var dialog: Dialog

    private lateinit var imageFileUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        // Check for permission to read external storage
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupImagePicker()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }


        binding.saveBtn.setOnClickListener{

            showProgressBar()
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val bio = binding.etBio.text.toString()

            val user = User(firstName, lastName, bio)
            if(uid != null){
                databaseReference.child(uid).setValue(user).addOnCompleteListener{

                    if(it.isSuccessful){
                        uploadProfilePic()
                    }else{

                        hideProgressBar()
                        Toast.makeText(this@MainActivity, "Failed to update profile",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        setContentView(R.layout.activity_main)

    }

    private fun setupImagePicker() {
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageFileUri = it
                Glide.with(this).load(uri).into(binding.profileImage)
            }
        }    }


    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        //pickImageLauncher.launch(intent)
    }

//    private val pickImageLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_OK && result.data != null) {
//                val selectedImageUri = result.data?.data
//                selectedImageUri?.let {
//                    // Perform cropping if needed
//                    val options = UCrop.Options()
//                    options.setCompressionQuality(70)
//                    val destinationUri: Uri =
//                        Uri.fromFile(File(cacheDir, "IMG_${System.currentTimeMillis()}.jpg"))
//                    UCrop.of(selectedImageUri, destinationUri)
//                        .withOptions(options)
//                        .start(this)
//                }
//            }
//        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                setupImagePicker()
            } else {
                Toast.makeText(
                    this,
                    "Permission to access storage denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }




    private fun uploadProfilePic() {
        imageUri = Uri.parse("android.resource://$packageName/${R.drawable.profile}")
        storageReference = FirebaseStorage.getInstance().getReference("Users/"+auth.currentUser?.uid)
        storageReference.putFile(imageUri).addOnSuccessListener {

            hideProgressBar()
            Toast.makeText(this@MainActivity, "profile successfuly updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{

            hideProgressBar()
            Toast.makeText(this@MainActivity, "Failed to updated the image ", Toast.LENGTH_SHORT).show()

        }
    }
    private fun showProgressBar(){

        dialog = Dialog(this@MainActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
    private fun hideProgressBar(){
        dialog.dismiss()
    }
}