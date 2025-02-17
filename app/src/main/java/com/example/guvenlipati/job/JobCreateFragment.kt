import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guvenlipati.R
import com.example.guvenlipati.adapter.SelectPetsAdapter
import com.example.guvenlipati.databinding.FragmentJobCreateBinding
import com.example.guvenlipati.home.HomeActivity
import com.example.guvenlipati.job.JobsActivity
import com.example.guvenlipati.models.Pet
import com.example.guvenlipati.models.User
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class JobCreateFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReferencePets: DatabaseReference
    private var petSelectID: String = ""
    private lateinit var user: User
    private lateinit var binding: FragmentJobCreateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJobCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectDateButton = binding.selectDateButton
        val petRecyclerView = binding.petRecycleView
        val editTextStartDate = binding.editTextStartDate
        val editTextEndDate = binding.editTextEndDate
        val jobStay = binding.job1
        val jobFeed = binding.job2
        val jobWalk = binding.job3
        val jobAbout = binding.editTextJobAbout

        auth = FirebaseAuth.getInstance()
        firebaseUser = auth.currentUser!!
        databaseReferencePets = FirebaseDatabase.getInstance().getReference("pets")


        petRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val selectPetList = ArrayList<Pet>()

        val databaseReferenceUsers =
            FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.uid)
        databaseReferenceUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userData = snapshot.getValue(User::class.java)
                    user = userData ?: User()

                    databaseReferencePets.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            selectPetList.clear()
                            for (dataSnapShot: DataSnapshot in snapshot.children) {
                                val pet = dataSnapShot.getValue(Pet::class.java)
                                if (pet?.userId == firebaseUser.uid) {
                                    pet.let {
                                        selectPetList.add(it)
                                    }
                                }
                            }

                            val petAdapter =
                                SelectPetsAdapter(
                                    requireContext(),
                                    selectPetList
                                ) { selectedPetId ->
                                    petSelectID = selectedPetId
                                }
                            petRecyclerView.adapter = petAdapter
                            if (petRecyclerView.adapter == petAdapter) {
                                binding.loadingCardView.visibility = View.GONE
                                binding.scrollView.foreground = null
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            showToast("Dostlar alınırken bir hata oluştu.")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Kullanıcı bilgileri alınırken bir hata oluştu.")
            }
        })

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())

        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Tarih Aralığını Seç")
                .setCalendarConstraints(constraintsBuilder.build())
                .setTheme(R.style.MyDatePickerTheme)
                .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second

            editTextStartDate.setText(formatDate(startDate))
            editTextEndDate.setText(formatDate(endDate))
        }

        selectDateButton.setOnClickListener {
            dateRangePicker.show(childFragmentManager, "date_range_picker")
        }


        var checkedJobType = ""

        jobStay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkedJobType = "homeJob"
            }
        }

        jobFeed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkedJobType = "feedingJob"
            }
        }

        jobWalk.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkedJobType = "walkingJob"
            }
        }


        binding.JobOptionButton.setOnClickListener {

            if (auth.currentUser != null) {

                (activity as JobsActivity).controllerIf(binding.editTextJobAbout, "Lütfen açıklama giriniz!")

                (activity as JobsActivity).controllerIf(binding.editTextStartDate, "Lütfen tarih seçiniz!")

                (activity as JobsActivity).controllerIf(binding.editTextEndDate, "Lütfen tarih seçiniz!")

                if (checkedJobType.trim().isEmpty()) {
                    showToast("Lütfen hizmet türü seçiniz!")
                    return@setOnClickListener
                }
                if (petSelectID.trim().isEmpty()) {
                    showToast("Lütfen dostunuzu seçiniz!")
                    return@setOnClickListener
                }

                databaseReferencePets.child(petSelectID).child("petSpecies")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val petSpecies = snapshot.getValue(String::class.java)
                                val hashMap: HashMap<String, Any> = HashMap()
                                val jobId = UUID.randomUUID().toString()
                                hashMap["jobId"] = jobId
                                hashMap["jobType"] = checkedJobType
                                hashMap["petSpecies"] = petSpecies.toString()
                                hashMap["jobAbout"] = jobAbout.text.toString()
                                hashMap["jobProvince"] = user.userProvince
                                hashMap["jobStatus"] = true
                                hashMap["userID"] = firebaseUser.uid
                                hashMap["petID"] = petSelectID
                                hashMap["jobTown"] = user.userTown
                                hashMap["jobStartDate"] = editTextStartDate.text.toString()
                                hashMap["jobEndDate"] = editTextEndDate.text.toString()

                                val reference =
                                    FirebaseDatabase.getInstance().getReference("jobs").child(jobId)
                                reference.setValue(hashMap).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        showBottomSheet()
                                    } else {
                                        showToast("Hatalı işlem!")
                                    }
                                }
                            } else {
                                showToast("Pet türü bulunamadı.")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            showToast("Pet türü alınırken bir hata oluştu.")
                        }
                    })
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showMaterialDialog()
        }
    }

    private fun showMaterialDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Emin Misiniz?")
            .setMessage("Eğer geri dönerseniz iş kaydınız silinecektir.")
            .setBackground(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.background_dialog
                )
            )
            .setPositiveButton("Geri Dön") { _, _ ->
                showToast("Kaydınız iptal edildi.")
                requireActivity().finish()
            }
            .setNegativeButton("İptal") { _, _ ->
                showToast("İptal Edildi")
            }
            .show()
    }

    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.bottomsheet_job_create, null)
        val backToMainButton = view.findViewById<Button>(R.id.backToMain)
        backToMainButton.setOnClickListener {
            requireActivity().finish()
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }
}
