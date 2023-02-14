package com.onlyforadventure.DocApp.mainFragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ncorti.slidetoact.SlideToActView
import com.onlyforadventure.DocApp.*
import com.onlyforadventure.DocApp.R
import com.onlyforadventure.DocApp.appointment.AppointmentBooking
import com.onlyforadventure.DocApp.databinding.FragmentHomeBinding


class HomeFragment : Fragment()  {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: DatabaseReference


    lateinit var databaseReference: DatabaseReference
    lateinit var userAdapter: userAdapter
    lateinit var list: ArrayList<userModel>

    //Current User's data
    private lateinit var userName : String
    private lateinit var userEmail : String
    private lateinit var userPhone : String
    private lateinit var userPosition: String
    private lateinit var userType: String
    private lateinit var userID: String
    private var userPrescription: String = "false"
    public var flag=0

    //Searched doctor's data
    private lateinit var searchedName : String
    private lateinit var searchedEmail : String
    private lateinit var searchedPhone : String
    private lateinit var searchedData : String
    private lateinit var searchedUID : String
    private lateinit var searchedType: String

    private lateinit var sharedPreference : SharedPreferences
    private lateinit var tagged : String



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser


        val items = listOf("All","Cardiologist", "Dentist", "ENT specialist", "Obstetrician/Gynaecologist", "Orthopaedic surgeon","Psychiatrists", "Radiologist", "Pulmonologist", "Neurologist", "Allergists", "Gastroenterologists", "Urologists", "Otolaryngologists", "Rheumatologists", "Anesthesiologists")
        db = FirebaseDatabase.getInstance().reference
        sharedPreference = requireActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE)

        getDataFromSharedPreference()

//        binding.addPrescription.setOnClickListener {
//            startActivity(Intent(context, AddPrescriptionActivity::class.java))
//            db.child("Users").child(userID).child("Prescription").addValueEventListener(object :ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val presURL = snapshot.child("fileurl").value.toString().trim()
//                    val editor = sharedPreference.edit()
//                    editor.putString("prescription", presURL)
//                    userPrescription = sharedPreference.getString("prescription", "false").toString().trim()
//                    editor.apply()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//            })
//        }

        val adapter = ArrayAdapter(requireActivity(), R.layout.list_items, items)



        binding.doctorData.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                // Call your code here
                searchedData = binding.doctorData.text.toString().trim()

                if (searchedData.isNotEmpty()) {
                    if (RemoveCountryCode.remove(searchedData) == userPhone || searchedData == userPhone || searchedData == userEmail || isSameName(searchedData, userName)) {
                        Toast.makeText(requireActivity(), "Stop searching yourself", Toast.LENGTH_SHORT).show()
                        binding.cardView.isVisible = false
                        binding.slider.isVisible = false
                    }else {
                        doctorIsPresent()
                    }
                } else {
                    Toast.makeText(requireActivity(), "Enter doctor's email / phone", Toast.LENGTH_SHORT).show()
                }
                true
            }
            false
        }


        binding.slider.animDuration = 150
        binding.slider.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                if (userPrescription != "false") {
                    val intent =  Intent(requireActivity(), AppointmentBooking::class.java)
                    intent.putExtra("Duid", searchedUID)
                    intent.putExtra("Dname", searchedName)
                    intent.putExtra("Demail", searchedEmail)
                    intent.putExtra("Dphone", searchedPhone)
                    intent.putExtra("Dtype", searchedType)
                    startActivity(intent)
                    binding.slider.resetSlider()
                } else {
                    Toast.makeText(requireActivity(), "Please upload your prescription in settings tab", Toast.LENGTH_SHORT).show()
                    binding.slider.resetSlider()
                }

            }
        }

        binding.allview?.setOnClickListener{
            specialise(binding.allview!!.getTag().toString())
        }
        binding.cardioView?.setOnClickListener{
            specialise(binding.cardioView!!.getTag().toString())
        }
        binding.orthoView?.setOnClickListener{
            specialise(binding.orthoView!!.getTag().toString())
        }
        binding.dentistView?.setOnClickListener{
            specialise(binding.dentistView!!.getTag().toString())
        }
        binding.entView?.setOnClickListener{
            specialise(binding.entView!!.getTag().toString())
        }
        binding.gynacView?.setOnClickListener{
            specialise(binding.gynacView!!.getTag().toString())
        }
        binding.psychoView?.setOnClickListener{
            specialise(binding.psychoView!!.getTag().toString())
        }
        binding.radioView?.setOnClickListener{
            specialise(binding.radioView!!.getTag().toString())
        }
        binding.pulmonoView?.setOnClickListener{
            specialise(binding.pulmonoView!!.getTag().toString())
        }
        binding.neuroView?.setOnClickListener{
            specialise(binding.neuroView!!.getTag().toString())
        }
        binding.allergView?.setOnClickListener{
            specialise(binding.allergView!!.getTag().toString())
        }
        binding.gastroView?.setOnClickListener{
            specialise(binding.gastroView!!.getTag().toString())
        }
        binding.uroView?.setOnClickListener{
            specialise(binding.uroView!!.getTag().toString())
        }
        binding.otoView?.setOnClickListener{
            specialise(binding.otoView!!.getTag().toString())
        }
        binding.rheumView?.setOnClickListener{
            specialise(binding.rheumView!!.getTag().toString())
        }
        binding.anesthView?.setOnClickListener{
            specialise(binding.anesthView!!.getTag().toString())
        }

        binding.locationData?.setOnClickListener{
            val intent=Intent(requireActivity(),choose::class.java)
            startActivity(intent)
        }

        return binding.root
    }




    private fun doctorIsPresent() {

        db.child("Doctor").addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (child in dataSnapshot.children) {
                    val map = child.value as HashMap<*, *>
                    val sName = map["name"].toString().trim()
                    val sType = map["specialist"].toString().trim()
                    val sEmail = map["email"].toString().trim()
                    val sPhone = map["phone"].toString().trim()
                    val sUid = map["uid"].toString().trim()
                    if (searchedData == sEmail || searchedData == sPhone || searchedData.trim() == sName || isSameName(searchedData,sName) || RemoveCountryCode.remove(searchedData) == sPhone) {
                        searchedName = sName
                        searchedEmail = sEmail
                        searchedPhone = sPhone
                        searchedUID = sUid
                        searchedType = sType
                        binding.textView3.isVisible = false
                        binding.cardView.isVisible = true
                        binding.slider.isVisible = true
                        binding.doctorName.text = "Name: Dr. $sName"
                        binding.doctortype.text = "Specialization: $sType"
                        binding.doctorEmail.text = "Email: $sEmail"
                        binding.doctorPhone.text = "Phone: $sPhone"
                        return
                    } else
                        binding.textView3.isVisible = true
                }
            }

            override fun onCancelled(error: DatabaseError) {}
            fun onCancelled(firebaseError: FirebaseError?) {}
        })
    }
    private fun isSameName(searchedName: String, dbNAME: String): Boolean {
        val modSearched: String = searchedName.replace(" ", "").toString().trim()
        val modDB: String = dbNAME.replace(" ", "").toString().trim()
        return modSearched == modDB;
    }

    override fun onStart() {
        super.onStart()
        Handler().postDelayed({
            getDataFromSharedPreference()
        }, 1000)
    }

     fun specialise(tag:String) {
        val intent = Intent(requireActivity(), displayDoctor::class.java)
        intent.putExtra("tag", tag)
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n", "CommitPrefEdits")
    private fun getDataFromSharedPreference() {
        userID = sharedPreference.getString("uid","Not found").toString()
        userName = sharedPreference.getString("name","Not found").toString()
        userEmail = sharedPreference.getString("email","Not found").toString()
        userPhone = sharedPreference.getString("phone","Not found").toString()
        userPosition = sharedPreference.getString("isDoctor", "Not found").toString()
        userPrescription = sharedPreference.getString("prescription", "false").toString()

        if (userPosition == "Doctor")
            binding.namePreview.text = "Dr. $userName"
        else
            binding.namePreview.text = userName
    }

}


