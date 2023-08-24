package com.example.myapplication

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAddDestinationBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class AddDestination : AppCompatActivity() {
    private lateinit var binding: ActivityAddDestinationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backFromNewDestination.setOnClickListener {
            goBackToMainView()
        }
        binding.applyNewDestination.setOnClickListener {
            val titleInput = binding.newDestinationTitleInput.text.toString()
            val timeInput = binding.NewDestinationTimeInput.text.toString()
            val descriptionInput = binding.newDestinationDescriptionInput.text.toString()
            val calendar = Calendar.getInstance()
            val formattedMonth = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val today = "${calendar.get(Calendar.DAY_OF_MONTH)}.${formattedMonth}.${ calendar.get(Calendar.YEAR)}"
            findSlot(titleInput, timeInput, descriptionInput, today)
        }
    }

    private fun parseDuration(duration: String): Int {
        return duration.split(" ")[0].toInt()
    }

    private fun findSlot(title: String, time: String, description: String, today: String ) {
        val myUserPath = DatabaseHandler.getUserReference()
        val activitiesReference: DatabaseReference = myUserPath

        activitiesReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val activityList = mutableListOf<ActivityModel>()
                var filteredActivityList = mutableListOf<ActivityModel>()
                val finishWork = dataSnapshot.child("stopWork").getValue(String::class.java)
                val sleepTime = dataSnapshot.child("sleepTime").getValue(String::class.java)
                val newActivityDuration = parseDuration(time)
                var freeSlotStart: String? = null
                var freeSlotEnd: String? = null

                if (sleepTime != null) {
                    if (finishWork != null) {
                        if (time.toInt()/60 > (sleepTime.split(":")[0].toInt() - finishWork.split(":")[0].toInt())) {
                            showToast("You are not able to to this task at once, split it to smaller subtasks.")
                            return
                        }
                    }
                }

                for (activitySnapshot in dataSnapshot.children) {
                    if (activitySnapshot.child("date").getValue(String::class.java) == today) {
                        val startHour = activitySnapshot.child("startTime").getValue(String::class.java)
                        val stopHour = activitySnapshot.child("stopTime").getValue(String::class.java)
                        val title = activitySnapshot.child("title").getValue(String::class.java)
                        val description =
                            activitySnapshot.child("description").getValue(String::class.java)

                        val activity = ActivityModel(title, today, startHour, stopHour, description, key = null, userKey = null)
                        activityList.add(activity)
                    }
                }

                activityList.add(ActivityModel("Sleep", today, sleepTime, startWork, "you sleep then", key = null, userKey = null))
                activityList.add(ActivityModel("Work", today, startWork, stopWork, "you work then ", key = null, userKey = null))

                activityList.sortBy {
                    val (hours, minutes) = it.startTime!!.split(":").map { it.toInt() }
                    hours * 60 + minutes}

                filteredActivityList = activityList

                for (i in 0 until filteredActivityList.size) {
                    if (freeSlotStart == null && freeSlotEnd == null) {
                        val currentActivity = filteredActivityList[i]
                        val nextActivity = filteredActivityList[i + 1]

                        val currentHour = currentActivity.stopTime!!.split(":")[0].toInt()
                        val currentMinute = currentActivity.stopTime.split(":")[1].toInt()

                        val nextHour = nextActivity.startTime!!.split(":")[0].toInt()
                        val nextMinute = nextActivity.startTime.split(":")[1].toInt()

                        val timeDifference =
                            (nextHour - currentHour) * 60 + (nextMinute - currentMinute)

                        if (timeDifference >= newActivityDuration) {
                            freeSlotStart = currentActivity.stopTime
                            freeSlotEnd = String.format(
                                "%02d:%02d",
                                currentHour,
                                currentMinute + newActivityDuration
                            )
                            while (freeSlotEnd!!.split(":")[1].toInt() >= 60) {
                                val newHour = freeSlotEnd.split(":")[0].toInt() + 1
                                val newMinute = freeSlotEnd.split(":")[1].toInt() - 60
                                freeSlotEnd = "$newHour:$newMinute"
                            }
                        }

                    }
                }


                if (freeSlotStart != null && freeSlotEnd != null) {
                    val confirmationDialog = AlertDialog.Builder(this@AddDestination)
                        .setTitle("Free slot found between $freeSlotStart - $freeSlotEnd $today")
                        .setMessage("Do you want to add it as the new activity to your calendar?")
                        .setPositiveButton("Yes") { _, _ ->
                            applyNewActivity(title, today, freeSlotStart, freeSlotEnd, description)
                        }
                        .setNegativeButton("No") { _, _ ->
                            binding.newDestinationTitleInput.text.clear()
                            binding.NewDestinationTimeInput.text.clear()
                            binding.newDestinationDescriptionInput.text.clear()
                        }
                        .create()

                    confirmationDialog.show()
                } else {
                    val confirmationDialog = AlertDialog.Builder(this@AddDestination)
                        .setTitle("No slot found for today")
                        .setMessage("Do you want to search ine in next day?")
                        .setPositiveButton("Yes") { _, _ ->
                            var day = today.split('.')[0].toInt() +1
                            var month = today.split('.')[1].toInt()
                            var year = today.split('.')[2].toInt()
                            if (day > 30){
                                day = 1
                                month += 1
                            }
                            if(month > 12){
                                month = 1
                                year += 1
                            }
                            val formattedMonth = (month).toString().padStart(2, '0')
                            val newDate = "$day.$formattedMonth.$year"
                            findSlot(title, time, description, newDate)
                        }
                        .setNegativeButton("No") { _, _ ->
                            binding.newDestinationTitleInput.text.clear()
                            binding.NewDestinationTimeInput.text.clear()
                            binding.newDestinationDescriptionInput.text.clear()
                        }
                        .create()

                    confirmationDialog.show()

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", databaseError.toException())
            }
        })

    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun applyNewActivity(title: String, date: String, startTime: String, stopTime: String, description: String ) {
        val myUserPath = DatabaseHandler.firebaseDatabaseReference.child("users")
        val userId = DatabaseHandler.firebaseAuth.currentUser?.uid

        userId?.let { uid ->
        val userPath = myUserPath.child(uid )
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
    }}

    private fun goBackToMainView() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}