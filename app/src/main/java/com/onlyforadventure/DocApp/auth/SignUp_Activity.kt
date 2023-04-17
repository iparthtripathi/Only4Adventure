package com.onlyforadventure.DocApp.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.onlyforadventure.DocApp.R
import com.onlyforadventure.DocApp.RemoveCountryCode
import com.onlyforadventure.DocApp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class SignUp_Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var fd: FirebaseDatabase
    private lateinit var img: Uri
    private lateinit var isDoctor: String
    private lateinit var age: String
    var flag=0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isDoctor = intent.extras!!.getString("isDoctor").toString()
        age = intent.extras!!.getString("age").toString()
        binding.profilePic.setOnClickListener{
            val intent= Intent()
            flag=1
            intent.action=Intent.ACTION_GET_CONTENT
            intent.type="image/*"
            startActivityForResult(intent,1)
        }
        initialization()

        if (isDoctor == "Doctor"){
            binding.menuDoctorType.visibility = View.VISIBLE
            binding.location.visibility=View.VISIBLE
            binding.license.visibility=View.VISIBLE
        }

        // Hide and Show Password
        var passwordVisible = false
        binding.SignUpPassword.setOnTouchListener { v, event ->
            val Right = 2
            if (event.getAction() === MotionEvent.ACTION_UP) {
                if (event.getRawX() >= binding.SignUpPassword.getRight() - binding.SignUpPassword.getCompoundDrawables()
                        .get(Right).getBounds().width()
                ) {
                    val selection: Int = binding.SignUpPassword.getSelectionEnd()
                    //Handles Multiple option popups
                    if (passwordVisible) {
                        //set drawable image here
                        binding.SignUpPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.visibility_off,
                            0
                        )
                        //for hide password
                        binding.SignUpPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                        passwordVisible = false
                    } else {
                        //set drawable image here
                        binding.SignUpPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.visibility,
                            0
                        )
                        //for show password
                        binding.SignUpPassword.setTransformationMethod(
                            HideReturnsTransformationMethod.getInstance())
                        passwordVisible = true
                    }
                    binding.SignUpPassword.setLongClickable(false) //Handles Multiple option popups
                    binding.SignUpPassword.setSelection(selection)
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.toSignIn.setOnClickListener {
            val intent = Intent(this, SignIn_Activity::class.java)
            startActivity(intent)
        }

        // Disease List
        val items = listOf("Cardiologist", "Dentist", "ENT specialist", "Obstetrician/Gynaecologist", "Orthopaedic surgeon","Psychiatrists", "Radiologist", "Pulmonologist", "Neurologist", "Allergists", "Gastroenterologists", "Urologists", "Otolaryngologists", "Rheumatologists", "Anesthesiologists")
        val adapter = ArrayAdapter(this, R.layout.list_items, items)
        binding.SignUpTypeOfDoctor.setAdapter(adapter)

        binding.createAccount.setOnClickListener {
            val sReference=storage.reference.child("Profile").child(Date().time.toString())
            if(flag==1) {
                sReference.putFile(img).addOnCompleteListener {
                    if (it.isSuccessful) {
                        sReference.downloadUrl.addOnSuccessListener { task ->
                            uploadInfo(task.toString())
                        }
                    }
                }
            }
        }
    }

    private fun uploadInfo(imgUrl: String) {

        if (binding.SignUpName.text.toString().trim().isNotEmpty() && binding.SignUpEmail.text.toString().trim().isNotEmpty() && binding.SignUpPassword.text.toString().trim().isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(binding.SignUpEmail.text.toString().trim()).matches()) {
            if (binding.SignUpPassword.text.toString().trim().length > 7) {
                firebaseAuth.createUserWithEmailAndPassword(binding.SignUpEmail.text.toString().trim(), binding.SignUpPassword.text.toString().trim()).addOnCompleteListener {
                    if (it.isSuccessful) {

                        val u = firebaseAuth.currentUser
                        val uid = firebaseAuth.currentUser?.uid.toString()
                        //Create user object
                        val statsData = "0:0:0:0:0?0:0:0:0:0?0:0:0:0:0?0:0:0:0:0"
                        val user = User(binding.SignUpName.text.toString().trim(), binding.SignUpEmail.text.toString().trim(),
                            RemoveCountryCode.remove(binding.SignUpPhone.text.toString().trim()), uid, isDoctor, age, binding.SignUpTypeOfDoctor.text.toString().trim(),"false", imgUrl, statsData, "false")

                        //add user data in the Realtime Database
                        db.child(u?.uid!!).setValue(user).addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                db.child(u.uid).child("verified").child("verfied").setValue("false")
                                u.sendEmailVerification()
                                Toast.makeText(this, "Email Verification sent to your mail", Toast.LENGTH_LONG).show()
                                val intent=Intent(this,enterMobile::class.java)
                                intent.putExtra("mobile",RemoveCountryCode.remove(binding.SignUpPhone.text.toString().trim()))
                                startActivity(intent)

                                if (isDoctor == "Doctor") {
                                    fd.getReference(isDoctor).child(u.uid).setValue(user).addOnSuccessListener {
                                    }

                                }

                            } else
                                Log.e("Not successful", "Unsuccessful")
                        }
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Password is not matching!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please enter the details!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data!=null){
            if(data.data!=null){
                img= data.data!!
                binding.profilePic.setImageURI(img)
            }
        }
    }

    private fun initialization() {
        supportActionBar?.hide()
        firebaseAuth = FirebaseAuth.getInstance()
        fd = FirebaseDatabase.getInstance()
        db = fd.getReference("Users")
        storage= FirebaseStorage.getInstance()
    }
}