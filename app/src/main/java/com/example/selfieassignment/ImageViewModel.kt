package com.example.selfieassignment

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ImageViewModel(application: Application) : AndroidViewModel(application) {
    private val databaseReference = FirebaseDatabase.getInstance().getReference()
    private val _images = MutableLiveData<List<Image>>()
    val images: LiveData<List<Image>> get() = _images
    private val auth: FirebaseAuth = Firebase.auth


    fun fetchImages() {
        val userId = auth.currentUser?.uid ?: return // If there's no signed-in user, return early
        // Makes sure to reference the "images" node under the users node
        databaseReference.child("images").child(userId).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageList = mutableListOf<Image>()
                for (postSnapshot in snapshot.children) {
                    val image = postSnapshot.getValue(Image::class.java)
                    image?.let {

                        it.id = postSnapshot.key ?: ""
                        imageList.add(it)
                    }
                }
                _images.value = imageList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "loadPost:onCancelled", error.toException())
                // Handle error
            }
        })
    }
    fun saveImage(imageUri: Uri) {
        //val userId = FirebaseAuth.getInstance().currentUser?.uid
        //val storageReference = FirebaseStorage.getInstance().reference.child("images/$userId/$imageName")
        // Get the current user's UID
        val userId = auth.currentUser?.uid ?: return // If there's no signed in user, return early

        val imageName = imageUri.lastPathSegment ?: "image_${System.currentTimeMillis()}"
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$userId/$imageName")

        // upload the image to Firebase Storage under the user's folder
        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // get the download URL
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    // Save the image data to Firebase Realtime Database under the users node
                    saveImageUrlToDatabase(downloadUrl, userId)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any upload errors
                Log.e(ContentValues.TAG, "Image upload failed", exception)
            }
    }

    private fun saveImageUrlToDatabase(downloadUrl: String, userId: String) {
        val key = databaseReference.child("images").child(userId).push().key ?: return // Generate a unique key for the image

        val image = Image(
            id = key,
            imageUrl = downloadUrl,
            timestamp = System.currentTimeMillis(),
            userId = userId // The UID of the user
        )

        // Saves the image details under the user's node in the Realtime Database
        databaseReference.child("images").child(userId).child(key).setValue(image)
            .addOnSuccessListener {
                // Image details successfully saved in the database
                Log.d(ContentValues.TAG, "Image URL saved in the database")
            }
            .addOnFailureListener {
                // Handle any errors saving data to the database
                Log.e(ContentValues.TAG, "Failed to save image URL to the database", it)
            }
    }

}