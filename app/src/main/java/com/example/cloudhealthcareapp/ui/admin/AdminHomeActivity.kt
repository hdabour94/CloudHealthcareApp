package com.example.cloudhealthcareapp.ui.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.cloudhealthcareapp.R

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var manageUsersButton: Button
    private lateinit var manageBillingButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        manageUsersButton = findViewById(R.id.manageUsersButton)
        manageBillingButton = findViewById(R.id.manageBillingButton)

        manageUsersButton.setOnClickListener {
            startActivity(Intent(this, ManageUsersActivity::class.java))
        }

        manageBillingButton.setOnClickListener {
            startActivity(Intent(this, BillingActivity::class.java))
        }
    }
}