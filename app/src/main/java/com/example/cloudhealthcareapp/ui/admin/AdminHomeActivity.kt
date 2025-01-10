package com.example.cloudhealthcareapp.ui.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.ui.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var manageUsersButton: Button
    private lateinit var manageBillingButton: Button
    private lateinit var signOutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        manageUsersButton = findViewById(R.id.manageUsersButton)
        manageBillingButton = findViewById(R.id.manageBillingButton)
        signOutButton = findViewById(R.id.signOutButton)

        manageUsersButton.setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }

        manageBillingButton.setOnClickListener {
            startActivity(Intent(this, BillingActivity::class.java))
        }

        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}