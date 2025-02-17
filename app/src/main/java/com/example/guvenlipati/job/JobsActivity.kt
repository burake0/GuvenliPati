package com.example.guvenlipati.job

import JobCreateFragment
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.guvenlipati.R
import com.example.guvenlipati.home.HomeActivity
import com.example.guvenlipati.home.ProfileFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class JobsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job)

        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainerView, JobCreateFragment()
            )
            .commit()

        val backToHome = findViewById<ImageButton>(R.id.backToHome)

        backToHome?.setOnClickListener{
            showMaterialDialog()
        }
    }
    private fun showMaterialDialog(){
        MaterialAlertDialogBuilder(this)
            .setTitle("Emin Misiniz?")
            .setMessage("Eğer geri dönerseniz iş kaydınız silinecektir.")
            .setBackground(ContextCompat.getDrawable(this, R.drawable.background_dialog))
            .setPositiveButton("Geri Dön") { _, _ ->
                showToast("Kaydınız iptal edildi.")
                this.finish()
            }
            .setNegativeButton("İptal") { _, _ ->
                showToast("İptal Edildi")
            }
            .show()
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun controllerIf(editText: EditText, message: String){
        if (editText.text.toString().trim().isEmpty()){
            showToast(message)
        }
    }
}