package com.example.guvenlipati.advert

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guvenlipati.OfferAdapter
import com.example.guvenlipati.databinding.FragmentPaymentAdvertBinding
import com.example.guvenlipati.models.Backer
import com.example.guvenlipati.models.Job
import com.example.guvenlipati.models.Offer
import com.example.guvenlipati.models.Pet
import com.example.guvenlipati.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PaymentAdvertFragment : Fragment() {

    lateinit var binding: FragmentPaymentAdvertBinding
    var ratings = 0.0
    var totalRatings = 0.0
    var sayac=0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentAdvertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val paymentAdvertRecyclerView = binding.paymentRecyclerView
        paymentAdvertRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        val jobList = ArrayList<Job>()
        val petList = ArrayList<Pet>()
        val userList = ArrayList<User>()
        val offerList = ArrayList<Offer>()
        val backerList = ArrayList<Backer>()

        val adapter = OfferAdapter(requireContext(), jobList, petList, userList, offerList, backerList, ratings)
        paymentAdvertRecyclerView.adapter = adapter

        FirebaseDatabase.getInstance().getReference("offers").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                offerList.clear()
                jobList.clear()
                petList.clear()
                userList.clear()
                backerList.clear()
                for (offerSnapshot in dataSnapshot.children) {
                    val offer = offerSnapshot.getValue(Offer::class.java)
                    if (offer != null && offer.offerUser == firebaseUser?.uid && isOfferWithinLast7Days(offer.offerDate) && !offer.offerStatus && !offer.priceStatus) {
                        offerList.add(offer)
                        FirebaseDatabase.getInstance().getReference("jobs").child(offer.offerJobId).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(jobSnapshot: DataSnapshot) {
                                val job = jobSnapshot.getValue(Job::class.java)
                                job?.let {
                                    jobList.add(it)

                                    FirebaseDatabase.getInstance().getReference("raiting").child(offer.offerId).addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(raitingSnapshot: DataSnapshot) {
                                            val raiting = raitingSnapshot.getValue(Double::class.java)
                                            raiting?.let {
                                                sayac++
                                                totalRatings += it
                                                ratings = totalRatings / sayac
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                        }
                                    })

                                    FirebaseDatabase.getInstance().getReference("pets").child(job.petID).addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(petSnapshot: DataSnapshot) {
                                            val pet = petSnapshot.getValue(Pet::class.java)
                                            pet?.let {
                                                petList.add(it)
                                                FirebaseDatabase.getInstance().getReference("users").child(offer.offerBackerId).addValueEventListener(object : ValueEventListener {
                                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                                        val user = userSnapshot.getValue(User::class.java)
                                                        user?.let {
                                                            userList.add(it)
                                                            FirebaseDatabase.getInstance().getReference("identifies").child(offer.offerBackerId).addValueEventListener(object : ValueEventListener {
                                                                override fun onDataChange(backerSnapshot: DataSnapshot) {
                                                                    val backer = backerSnapshot.getValue(Backer::class.java)
                                                                    backer?.let {
                                                                        backerList.add(it)
                                                                        adapter.notifyDataSetChanged()
                                                                        binding.loadingCardView.visibility=View.GONE
                                                                        binding.paymentRecyclerView.foreground=null
                                                                    }
                                                                }
                                                                override fun onCancelled(error: DatabaseError) {}
                                                            })
                                                        }
                                                    }
                                                    override fun onCancelled(error: DatabaseError) {}
                                                })
                                            }
                                        }
                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible database errors
            }
        })
    }

    private fun isOfferWithinLast7Days(offerDate: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val offerDateTime = dateFormat.parse(offerDate)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        return offerDateTime != null && offerDateTime.after(calendar.time)
    }
}
