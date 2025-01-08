package com.example.cloudhealthcareapp.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun formatDate(date: Date, format: String = Constants.DATE_FORMAT): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

    // Add more utility functions as needed (e.g., for input validation, formatting, etc.)
}