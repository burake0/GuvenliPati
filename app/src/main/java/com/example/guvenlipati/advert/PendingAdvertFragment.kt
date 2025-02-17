package com.example.guvenlipati.advert

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guvenlipati.AdvertsAdapter
import com.example.guvenlipati.databinding.FragmentPendingAdvertBinding
import com.example.guvenlipati.models.Job
import com.example.guvenlipati.models.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.collection.LLRBNode.Color
import java.text.SimpleDateFormat
import java.util.*

class PendingAdvertFragment : Fragment() {

    private lateinit var binding: FragmentPendingAdvertBinding
    private lateinit var adapter: AdvertsAdapter
    private val jobList = mutableListOf<Job>()
    private val petList = mutableListOf<Pet>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPendingAdvertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        fetchJobs(currentUser!!)
    }

    private fun setupRecyclerView() {
        adapter = AdvertsAdapter(requireContext(), jobList, petList)
        binding.pendingAdvertRecycleView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PendingAdvertFragment.adapter
        }
    }

    private fun fetchPets(job: Job) {
        FirebaseDatabase.getInstance().getReference("pets").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(petsSnapshot: DataSnapshot) {
                petsSnapshot.children.mapNotNull { it.getValue(Pet::class.java) }.forEach {
                    if (it.userId == job.userID) {
                        petList.add(it)
                        jobList.add(job)
                        adapter.notifyDataSetChanged()
                        binding.animationView2.visibility = View.GONE
                        binding.scrollView.foreground = null
                        binding.loadingCardView.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load pets: ${error.message}")
            }
        })
    }

    private fun fetchJobs(userId: String) {
        FirebaseDatabase.getInstance().getReference("jobs").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(jobsSnapshot: DataSnapshot) {
                jobList.clear()
                petList.clear()
                jobsSnapshot.children.mapNotNull { it.getValue(Job::class.java) }.forEach { job ->
                    job.jobStartDate.let { startDateStr ->
                        dateFormat.parse(startDateStr)?.let { startDate ->
                            if (!startDate.before(Date()) && job.userID == userId && job.jobStatus) {
                                binding.scrollView.foreground = ColorDrawable(android.graphics.Color.parseColor("#FFFFFF"))
                                binding.loadingCardView.visibility = View.GONE
                                fetchPets(job)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load jobs: ${error.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}