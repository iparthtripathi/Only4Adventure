package com.onlyforadventure.DocApp.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.onlyforadventure.DocApp.prescription.AddPrescriptionActivity
import com.onlyforadventure.DocApp.databinding.ActivityEditProfileBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var db : DatabaseReference
    private lateinit var db1 : DatabaseReference
    private lateinit var sharedPreference : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreference = baseContext.getSharedPreferences("UserData", Context.MODE_PRIVATE)

        binding.confirm.setOnClickListener {

            val name = binding.editName.text.toString()
            val age = binding.editAge.text.toString()
            val phoneno = binding.editPhoneNumber.text.toString()

            updateData(name,age,phoneno)
        }
        binding.updatepres.setOnClickListener {
            startActivity(Intent(baseContext, AddPrescriptionActivity::class.java))
        }
    }

    private fun updateData(name: String, age: String, phoneno: String) {

        var userID = sharedPreference.getString("uid", "Not found").toString()
        db = FirebaseDatabase.getInstance().getReference("Users")
        db1=FirebaseDatabase.getInstance().getReference("Doctor")
        val user = mapOf<String,String>(
            "name" to name,
            "age" to age,
            "phone" to phoneno
        )

        db.child(userID).updateChildren(user).addOnSuccessListener {

            binding.editName.text.clear()
            binding.editAge.text.clear()
            binding.editPhoneNumber.text.clear()
            Toast.makeText(baseContext, "Successfully Updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(baseContext, "Failed to update", Toast.LENGTH_SHORT).show()
        }
        db1.child(userID).updateChildren(user).addOnSuccessListener {

            binding.editName.text.clear()
            binding.editAge.text.clear()
            binding.editPhoneNumber.text.clear()
        }.addOnFailureListener {

        }
    }
}