package com.example.myapplication

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySettingsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class Settings : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        refreshView()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backFromSettings.setOnClickListener {
            goBackToMainView()
        }
        binding.applyNewSettings.setOnClickListener {
            applyNewActivity()
            refreshView()
        }

    }

    fun applyNewActivity() {
        val myUserPath = DatabaseHandler.firebaseDatabaseReference.child("users")
        val userId = DatabaseHandler.firebaseAuth.currentUser?.uid

        userId?.let { uid ->
            val userPath = myUserPath.child(uid)

            userPath.child("sleepTime").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val newSleepTime = binding.newSleepTimeInput.text.toString().trim()
                        if (newSleepTime.isNotEmpty()) {
                            userPath.child("sleepTime").setValue(newSleepTime)
                        }

                    } else {
                        val newSleepTime = binding.newSleepTimeInput.text.toString().trim()
                        val newValues = mapOf("sleepTime" to newSleepTime)
                        userPath.updateChildren(newValues)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error querying: ${error.message}")
                }
            })

            userPath.child("startWork").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val newStartWork = binding.newstartWorkInput.text.toString().trim()
                        if (newStartWork.isNotEmpty()) {
                            userPath.child("startWork").setValue(newStartWork)
                        }

                    } else {
                        val newStartWork = binding.newstartWorkInput.text.toString().trim()
                        val newValues = mapOf("startWork" to newStartWork)
                        userPath.updateChildren(newValues)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error querying: ${error.message}")
                }
            })

            userPath.child("stopWork").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val newStopWork = binding.newfinishWorkInput.text.toString().trim()
                        if (newStopWork.isNotEmpty()) {
                            userPath.child("stopWork").setValue(newStopWork)
                        }

                    } else {
                        val newStopWork = binding.newfinishWorkInput.text.toString().trim()
                        val newValues = mapOf("stopWork" to newStopWork)
                        userPath.updateChildren(newValues)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error querying: ${error.message}")
                }
            })
        }
    }

    private fun refreshView() {
        val myUserPath = DatabaseHandler.getUserReference()
        val activitiesReference: DatabaseReference? = myUserPath

        activitiesReference?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("startWork").exists()) {
                    val startWork = dataSnapshot.child("startWork").getValue(String::class.java)
                    binding.startWorkInput.text = startWork
                }
                else{
                    binding.startWorkInput.text = startWork
                }
                if (dataSnapshot.child("stopWork").exists()){
                    val stopWork = dataSnapshot.child("stopWork").getValue(String::class.java)
                    binding.finishWorkInput.text = stopWork
                }
                else{
                    binding.finishWorkInput.text = stopWork
                }
                if (dataSnapshot.child("sleepTime").exists()){
                    val sleepTime = dataSnapshot.child("sleepTime").getValue(String::class.java)
                    binding.sleepTimeInput.text = sleepTime
                }
                else{
                    binding.sleepTimeInput.text = sleepTime

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", databaseError.toException())
            }
        })
    }

    fun goBackToMainView() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}