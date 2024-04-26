package com.example.guvenlipati.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.guvenlipati.R
import com.example.guvenlipati.chat.ProfileActivity
import com.example.guvenlipati.models.Job
import com.example.guvenlipati.models.Offer
import com.example.guvenlipati.models.Pet
import com.example.guvenlipati.models.User

class PastJobAdapter(
    private val context: Context,
    private val jobList: List<Job>,
    private val petList: List<Pet>,
    private val userList: List<User>,
    private val offerList: MutableList<Offer>
) : RecyclerView.Adapter<PastJobAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_completed_job, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PastJobAdapter.ViewHolder, position: Int) {
        if (position < jobList.size && position < petList.size && position < userList.size && position < offerList.size) {
            holder.bind(
                jobList[position],
                petList[position],
                userList[position],
                offerList[position]
            )
        }
    }
    override fun getItemCount(): Int = offerList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val petPhotoImageView = view.findViewById<ImageView>(R.id.petPhotoImageView)
        private val petNameTextView = view.findViewById<TextView>(R.id.petNameTextView)
        private val jobTypeTextView = view.findViewById<TextView>(R.id.jobTypeTextView)
        private val petTypeTextView = view.findViewById<TextView>(R.id.petTypeTextView)
        private val startDateTextView = view.findViewById<TextView>(R.id.startDateTextView)
        private val endDateTextView = view.findViewById<TextView>(R.id.endDateTextView)
        private val locationTextView = view.findViewById<TextView>(R.id.locationTextView)
        private val backerPhotoImageView =
            view.findViewById<ImageView>(R.id.backerPhotoImageView)
        private val backerNameTextView = view.findViewById<TextView>(R.id.backerNameTextView)
        private val priceTextView = view.findViewById<TextView>(R.id.priceTextView)
        private val priceVergiTextView = view.findViewById<TextView>(R.id.priceVergiTextView)
        private val priceKomisyonTextView = view.findViewById<TextView>(R.id.priceKomisyonTextView)
        private val priceTotalTextView = view.findViewById<TextView>(R.id.priceTotalTextView)

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(job: Job, pet: Pet, user: User, offer: Offer) {
            when (job.jobType) {
                "feedingJob" -> jobTypeTextView.text = "Besleme"
                "walkingJob" -> jobTypeTextView.text = "Gezdirme"
                "homeJob" -> jobTypeTextView.text = "Evde Bakım"
            }
            val priceJob = offer.offerPrice.toDouble()
            val tax = priceJob * 0.18
            val komisyon = priceJob * 0.20
            val total = priceJob - tax - komisyon
            val formattedTax = String.format("%.1f", tax)
            val formattedKomisyon = String.format("%.1f", komisyon)
            val formattedTotal = String.format("%.1f", total)

            petNameTextView.text = pet.petName
            petTypeTextView.text = pet.petBreed
            startDateTextView.text = job.jobStartDate
            endDateTextView.text = job.jobEndDate
            locationTextView.text = job.jobProvince + ", " + job.jobTown
            backerNameTextView.text = user.userName + " " + user.userSurname
            priceTextView.text = offer.offerPrice.toString() + " TL"
            priceVergiTextView.text = "-$formattedTax TL"
            priceKomisyonTextView.text = "-$formattedKomisyon TL"
            priceTotalTextView.text = "$formattedTotal TL"


            Glide.with(context)
                .load(pet.petPhoto)
                .placeholder(R.drawable.default_pet_image_2)
                .into(petPhotoImageView)

            Glide.with(context)
                .load(user.userPhoto)
                .placeholder(R.drawable.default_pet_image_2)
                .into(backerPhotoImageView)

            backerPhotoImageView.setOnClickListener {
                val intent = Intent(context, ProfileActivity::class.java)
                intent.putExtra("userId", pet.userId)
                context.startActivity(intent)
            }

        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


}
