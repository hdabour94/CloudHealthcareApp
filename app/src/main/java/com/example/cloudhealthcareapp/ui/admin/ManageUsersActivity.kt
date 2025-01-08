package com.example.cloudhealthcareapp.ui.admin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.viewmodel.AdminViewModel

class ManageUsersActivity : AppCompatActivity() {

    private val viewModel: AdminViewModel by viewModels()
    private lateinit var userIdEditText: EditText
    private lateinit var activateButton: Button
    private lateinit var deactivateButton: Button
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        userIdEditText = findViewById(R.id.userIdEditText)
        activateButton = findViewById(R.id.activateButton)
        deactivateButton = findViewById(R.id.deactivateButton)
        deleteButton = findViewById(R.id.deleteButton)

        activateButton.setOnClickListener {
            val userId = userIdEditText.text.toString()
            if (userId.isNotEmpty()) {
                // TODO: Implement user activation logic (e.g., update user status in Firestore)
                Toast.makeText(this, "Activate User - Not yet implemented", Toast.LENGTH_SHORT).show()
            }
        }

        deactivateButton.setOnClickListener {
            val userId = userIdEditText.text.toString()
            if (userId.isNotEmpty()) {
                // TODO: Implement user deactivation logic
                Toast.makeText(this, "Deactivate User - Not yet implemented", Toast.LENGTH_SHORT).show()
            }
        }

        deleteButton.setOnClickListener {
            val userId = userIdEditText.text.toString()
            if (userId.isNotEmpty()) {
                // TODO: Implement user deletion logic
                Toast.makeText(this, "Delete User - Not yet implemented", Toast.LENGTH_SHORT).show()
            }
        }
    }
}