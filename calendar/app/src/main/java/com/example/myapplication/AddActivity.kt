package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddBinding

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backFromNewActivity.setOnClickListener {
            goBackToMainView()
        }
        binding.applyNewActivity.setOnClickListener {
            val titleInput = binding.newActivityTitleInput.text.toString()
            val dateInput = binding.newActivityDateInput.text.toString()
            val startTimeInput = binding.newActivityStartTimeInput.text.toString()
            val stopTimeInput = binding.newActivityStopTimeInput.text.toString()
            val descriptionInput = binding.newActivityDescriptionInput.text.toString()
            applyNewActivity(titleInput, dateInput, startTimeInput, stopTimeInput, descriptionInput)
        }

        binding.allDayButton.setOnClickListener{
            binding.newActivityStartTimeInput.setText("00:00")
            binding.newActivityStopTimeInput.setText("23:59")
        }
    }

    fun applyNewActivity(title: String, date: String, startTime: String, stopTime: String, description: String ) {
        val myUserPath = DatabaseHandler.firebaseDatabaseReference.child("users")
        val userId = DatabaseHandler.firebaseAuth.currentUser?.uid

        userId?.let { uid ->
            val userPath = myUserPath.child(uid)
            val newActivityKey = userPath.push().key ?: return

            val activityData = hashMapOf(
                "title" to title,
                "date" to date,
                "startTime" to startTime,
                "stopTime" to stopTime,
                "description" to description
            )

            userPath.child(newActivityKey).setValue(activityData)
                .addOnSuccessListener {
                    println("success")
                    showToast("Activity added successfully!")
                    goBackToMainView()
                }
                .addOnFailureListener {
                    println("failure")
                    showToast("Failed to add activity")
                }
        }


    }
    fun goBackToMainView() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}