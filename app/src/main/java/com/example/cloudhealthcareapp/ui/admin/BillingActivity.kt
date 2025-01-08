package com.example.cloudhealthcareapp.ui.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.cloudhealthcareapp.R

class BillingActivity : AppCompatActivity() {

    private lateinit var patientIdEditText: EditText
    private lateinit var generateInvoiceButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_billing)

        patientIdEditText = findViewById(R.id.patientIdEditText)
        generateInvoiceButton = findViewById(R.id.generateInvoiceButton)

        generateInvoiceButton.setOnClickListener {
            val patientId = patientIdEditText.text.toString()
            if (patientId.isNotEmpty()) {
                // TODO: Implement invoice generation logic (e.g., calculate costs, generate PDF, etc.)
                Toast.makeText(this, "Generate Invoice - Not yet implemented", Toast.LENGTH_SHORT).show()
            }
        }
    }
}