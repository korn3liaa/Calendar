package com.example.myapplication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object DatabaseHandler {
    private val firebaseDatabase by lazy { Firebase.database("https://calendar-759dd-default-rtdb.europe-west1.firebasedatabase.app/") }
    val firebaseDatabaseReference = firebaseDatabase.reference

    val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    fun getUserReference(): DatabaseReference {
        val userId = firebaseAuth.currentUser?.uid
        return if (userId != null) {
            firebaseDatabase.reference.child("users").child(userId)
        } else {
            firebaseDatabase.reference.child("users/test")
        }
    }

}
