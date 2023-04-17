package com.onlyforadventure.DocApp.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.onlyforadventure.DocApp.HomeActivity
import com.onlyforadventure.DocApp.R
import com.onlyforadventure.DocApp.databinding.ActivitySignInBinding
import com.onlyforadventure.DocApp.encryptionHelper.Encryption
import com.onlyforadventure.DocApp.waitPage
import com.squareup.picasso.Picasso
import java.util.*

class SignIn_Activity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var auth: FirebaseAuth
    private lateinit var dbreference : DatabaseReference
    private lateinit var user: FirebaseUser
    lateinit var googleSignInOptions: GoogleSignInOptions
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var verified: String

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialization()
        val sharedPreference =  getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()

        auth = FirebaseAuth.getInstance()



        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        val google = findViewById<Button>(R.id.Google)
        google.setOnClickListener { SignIn() }
        // Hide and Show Password
        var passwordVisible = false
        binding.SignInPassword.setOnTouchListener { v, event ->
            val Right = 2
            if (event.getAction() === MotionEvent.ACTION_UP) {
                if (event.getRawX() >= binding.SignInPassword.getRight() - binding.SignInPassword.getCompoundDrawables().get(Right).getBounds().width()) {
                    val selection: Int = binding.SignInPassword.getSelectionEnd()
                    //Handles Multiple option popups
                    if (passwordVisible) {
                        //set drawable image here
                        binding.SignInPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibility_off, 0)
                        //for hide password
                        binding.SignInPassword.setTransformationMethod(PasswordTransformationMethod.getInstance())
                        passwordVisible = false
                    } else {
                        //set drawable image here
                        binding.SignInPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibility, 0)
                        //for show password
                        binding.SignInPassword.setTransformationMethod(
                            HideReturnsTransformationMethod.getInstance())
                        passwordVisible = true
                    }
                    binding.SignInPassword.setLongClickable(false) //Handles Multiple option popups
                    binding.SignInPassword.setSelection(selection)
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }

        binding.toSignUp.setOnClickListener {
            val intent = Intent(this, SignUp_First::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.SignInEmail.text.toString()
            val password = binding.SignInPassword.text.toString()
            binding.loginButton.visibility= View.GONE
            binding.progressbar.visibility=View.VISIBLE


            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (password.length > 7) {

                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val u = firebaseAuth.currentUser
                            if (true) {

                                val db = FirebaseDatabase.getInstance().reference
                                val encryption = Encryption.getDefault("Key", "Salt", ByteArray(16))

                                if (u != null) {
                                    db.child("Users").child(u.uid).addValueEventListener(object: ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {

                                            editor.putString("uid", snapshot.child("uid").value.toString().trim())
                                            editor.putString("name", snapshot.child("name").value.toString().trim())
                                            editor.putString("age", snapshot.child("age").value.toString().trim())
                                            editor.putString("email", snapshot.child("email").value.toString().trim())
                                            editor.putString("phone", snapshot.child("phone").value.toString().trim())
                                            editor.putString("isDoctor", snapshot.child("doctor").value.toString().trim())
                                            editor.putString("specialist",snapshot.child("specialist").value.toString().trim())
                                            editor.putString("stats", snapshot.child("stats").value.toString().trim())
                                            editor.putString("prescription", snapshot.child("prescription").value.toString().trim())
                                            editor.putString("upi", snapshot.child(encryption.encrypt("nulla")).value.toString().trim())
                                            editor.apply()

                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }
                                    })
                                }
                                binding.loginButton.visibility= View.VISIBLE
                                binding.progressbar.visibility=View.GONE
                                startActivity(Intent(this, HomeActivity::class.java))

                            } else {
                                if (u != null) {
                                    u.sendEmailVerification()
                                }
                                Toast.makeText(
                                    this,
                                    "Email Verification sent to your mail",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.loginButton.visibility= View.VISIBLE
                                binding.progressbar.visibility=View.GONE
                            }
                        } else
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        binding.loginButton.visibility= View.VISIBLE
                        binding.progressbar.visibility=View.GONE
                    }
                } else {
                    Toast.makeText(this, "Password length must be greater than 8", Toast.LENGTH_SHORT).show()
                    binding.loginButton.visibility= View.VISIBLE
                    binding.progressbar.visibility=View.GONE
                }

            } else {
                Toast.makeText(this, "Please enter the details!", Toast.LENGTH_SHORT).show()
                binding.loginButton.visibility= View.VISIBLE
                binding.progressbar.visibility=View.GONE
            }
        }
    }


    private fun SignIn() {
        val intent = googleSignInClient.signInIntent
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val googleSignInAccount = task.getResult(
                    ApiException::class.java
                )
                updateUI(googleSignInAccount)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(googleSignInAccount: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
        Objects.requireNonNull(googleSignInAccount.email).let {
            if (it != null) {
                auth.fetchSignInMethodsForEmail(it)
                    .addOnCompleteListener {
                        auth.signInWithCredential(credential).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                startActivity(Intent(applicationContext, HomeActivity::class.java))
                                finish()
                            }
                        }
                    }
            }
        }
    }


    private fun initialization() {
        supportActionBar?.hide()
        firebaseAuth = FirebaseAuth.getInstance()
    }
}