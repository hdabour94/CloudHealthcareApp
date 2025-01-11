package com.example.cloudhealthcareapp.ui.admin

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cloudhealthcareapp.R
import com.example.cloudhealthcareapp.models.User

class UsersAdapter(
    private val onActivateClick: (User) -> Unit,
    private val onDeactivateClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    private var users: List<User> = emptyList()

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userIdTextView: TextView = view.findViewById(R.id.userIdTextView)
        val fullNameTextView: TextView = view.findViewById(R.id.fullNameTextView)
        val emailTextView: TextView = view.findViewById(R.id.emailTextView)
        val userTypeTextView: TextView = view.findViewById(R.id.userTypeTextView)
        val isVerifiedTextView: TextView = view.findViewById(R.id.isVerifiedTextView)
        val activateButton: Button = view.findViewById(R.id.activateButton)
        val deactivateButton: Button = view.findViewById(R.id.deactivateButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.userIdTextView.text = "ID: ${user.userId}"
        holder.fullNameTextView.text = "Name: ${user.fullName}"
        holder.emailTextView.text = "Email: ${user.email}"
        holder.userTypeTextView.text = "Type: ${user.userType}"
        holder.isVerifiedTextView.text = "Verified: ${user.isVerified}"

        // Handle visibility of buttons based on user type and verification status
        if (user.userType == "Administrator") {
            holder.activateButton.visibility = View.GONE
            holder.deactivateButton.visibility = View.GONE
        } else {
            if (user.isVerified) {
                holder.activateButton.visibility = View.GONE
                holder.deactivateButton.visibility = View.VISIBLE
            } else {
                holder.activateButton.visibility = View.VISIBLE
                holder.deactivateButton.visibility = View.GONE
            }
        }

        holder.activateButton.setOnClickListener { onActivateClick(user) }
        holder.deactivateButton.setOnClickListener { onDeactivateClick(user) }
        holder.deleteButton.setOnClickListener { onDeleteClick(user) }

        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}