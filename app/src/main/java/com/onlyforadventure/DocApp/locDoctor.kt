package com.onlyforadventure.DocApp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.onlyforadventure.DocApp.mainFragments.HomeFragment
import java.util.*
import kotlin.collections.ArrayList

class locDoctor: AppCompatActivity(),SelectListener {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: DatabaseReference
    lateinit var databaseReference: DatabaseReference
    lateinit var userAdapter: userAdapter
    lateinit var list: ArrayList<userModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loc_doctor)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        db = FirebaseDatabase.getInstance().reference
        val loc= intent.extras!!.getString("loc")
        val userList=findViewById<RecyclerView>(R.id.userList)
        val locTextView=findViewById<TextView>(R.id.locationTextView)
        databaseReference = FirebaseDatabase.getInstance().getReference("Doctor")
        // recyclerView?.setHasFixedSize(true)

        list = ArrayList<userModel>()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (dataSnapshot in snapshot.children) {
                    if(loc== dataSnapshot.child("location").getValue(String::class.java)
                            ?.toLowerCase(Locale.ROOT)){
                        val userModel = userModel(
                            dataSnapshot.child("name").getValue(String::class.java),
                            dataSnapshot.child("email").getValue(String::class.java),
                            dataSnapshot.child("specialist").getValue(String::class.java),
                            dataSnapshot.child("phone").getValue(String::class.java),
                            dataSnapshot.child("imgUrl").getValue(String::class.java),
                            dataSnapshot.child("location").getValue(String::class.java),
                            dataSnapshot.child("fees").getValue(String::class.java),
                            dataSnapshot.child("uid").getValue(String::class.java)
                        )
                        list.add(userModel)
                        locTextView.setText("Location: "+loc)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        userList.layoutManager = LinearLayoutManager(this)

        userAdapter = userAdapter(this, list,this)
        userList.adapter = userAdapter
    }

    override fun onItemClicked(userModel: userModel?) {

        val intent=Intent(applicationContext,HomeFragment::class.java)
        intent.putExtra("name",userModel?.name)
        intent.putExtra("email",userModel?.email)
        intent.putExtra("phone",userModel?.phone)
        intent.putExtra("specialization",userModel?.specialization)
        intent.action = "yes"
        startActivity(intent)
    }

}

