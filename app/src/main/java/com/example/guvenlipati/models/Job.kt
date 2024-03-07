package com.example.guvenlipati.models

import java.time.LocalDate

data class Job(
    val jobType: String = "",
    val jobStartDate: String = "",
    val jobFinishDate: String = "",
    val petId: String = "",
    val userId: String = "",
    val jobAbout: String = "",
    val jobProvince: String = "",
    val jobTown: String = "",
    val jobStatus: Boolean = false
)
