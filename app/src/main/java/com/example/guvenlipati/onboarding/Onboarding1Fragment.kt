package com.example.guvenlipati.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.guvenlipati.R

class Onboarding1Fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_on_boarding1, container, false)
    }

    companion object {
        fun newInstance(): Onboarding1Fragment {
            return Onboarding1Fragment()
        }
    }
}
