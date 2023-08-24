package com.example.myapplication

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(localClassName, "onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        binding.delete.setOnClickListener {
            displayActivityListDialog()
        }


        val dateView: TextView? = findViewById(/* id = */ binding.currentSelectedDate.id) as? TextView
        val scrollLayout = binding.scrollLayout

        (findViewById(binding.calendarView.id) as? CalendarView)?.setOnDateChangeListener { _, year, month, dayOfMonth ->

            val formattedMonth = (month + 1).toString().padStart(2, '0')
            val date = "$dayOfMonth.${formattedMonth}.$year"

            if (dateView != null) {
                dateView.text = date
            }

            val myUserPath = DatabaseHandler.getUserReference()
            val activitiesReference: DatabaseReference = myUserPath

            activitiesReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val activityList = mutableListOf<ActivityModel>()


                    for (activitySnapshot in dataSnapshot.children) {
                        if (activitySnapshot.child("date").getValue(String::class.java) == date) {
                            val startHour = activitySnapshot.child("startTime").getValue(String::class.java)
                            val stopHour = activitySnapshot.child("stopTime").getValue(String::class.java)
                            val title = activitySnapshot.child("title").getValue(String::class.java)
                            val description =
                                activitySnapshot.child("description").getValue(String::class.java)

                            val activity = ActivityModel(title, date, startHour, stopHour, description, key = null, userKey = null)
                            activityList.add(activity)
                        }
                    }

                    activityList.sortBy {
                        val (hours, minutes) = it.startTime!!.split(":").map { it.toInt() }
                        hours * 60 + minutes}

                    val stringBuilder = StringBuilder()
                    for (activity in activityList) {
                        if (activity.description!!.isNotEmpty()) {
                            stringBuilder.append("Title: ${activity.title} \nTime: ${activity.startTime} - ${activity.stopTime}\nDescription: ${activity.description}\n\n")
                        } else {
                            stringBuilder.append("Title: ${activity.title} \nTime: ${activity.startTime} - ${activity.stopTime}\n\n")
                        }
                    }

                    val textView = TextView(this@MainActivity)
                    textView.text = stringBuilder.toString()
                    scrollLayout.removeAllViews()
                    scrollLayout.addView(textView)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", databaseError.toException())
                }
            })

        }



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater

        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.new_activity -> addNewActivity()
            R.id.new_destinantion -> addNewDestination()
            R.id.settings -> goToAppSettings()
            R.id.menu_log_out -> logOut()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logOut(): Boolean {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun goToAppSettings(): Boolean {
        val intent = Intent(this, Settings::class.java)
        startActivity(intent)
        return true
    }

    private fun addNewDestination(): Boolean {
        val intent = Intent(this, AddDestination::class.java)
        startActivity(intent)
        return true
    }

    private fun addNewActivity(): Boolean {
        val intent = Intent(this, AddActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun displayActivityListDialog() {
        val dateView: TextView? =
            findViewById(/* id = */ binding.currentSelectedDate.id) as? TextView

        val myUserPath = DatabaseHandler.getUserReference()
        val activitiesReference: DatabaseReference = myUserPath

        activitiesReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val activityList = mutableListOf<ActivityModel>()

                for (activitySnapshot in dataSnapshot.children) {
                    if (activitySnapshot.child("date").getValue(String::class.java) == dateView?.text.toString()) {
                        val startHour =
                            activitySnapshot.child("startTime").getValue(String::class.java)
                        val stopHour =
                            activitySnapshot.child("stopTime").getValue(String::class.java)
                        val title = activitySnapshot.child("title").getValue(String::class.java)
                        val description =
                            activitySnapshot.child("description").getValue(String::class.java)
                        val key = activitySnapshot.key // Get the activity key
                        val userKey = myUserPath.key // Get the user key

                        val activity = ActivityModel(
                            title,
                            dateView.toString(),
                            startHour,
                            stopHour,
                            description,
                            key,
                            userKey
                        )
                        activityList.add(activity)
                    }
                }
                val activityTitles = activityList.map { it.title }.toTypedArray()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose an activity to delete")
                    .setItems(activityTitles) { _, which ->
                        val selectedActivity = activityList[which]
                        displayConfirmationDialog(selectedActivity)
                    }
                    .show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException())
            }
        })
    }

    private fun displayConfirmationDialog(selectedActivity: ActivityModel) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Do you want to delete the selected activity?")
            .setPositiveButton("Delete") { _, _ ->
                deleteActivity(selectedActivity)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteActivity(activity: ActivityModel) {
        val myUserPath = DatabaseHandler.firebaseDatabaseReference.child("users")
        val userReference = activity.userKey?.let { myUserPath.child(it) }

        val activityReferenceToDelete = activity.key?.let { userReference?.child(it) }
        activityReferenceToDelete?.removeValue()?.addOnSuccessListener {
            showToast("Activity successfully deleted")
        }?.addOnFailureListener {
            showToast("Failed to delete activity")
        }
    }

    private fun showToast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

}
