package com.geovannycode.apirestcoroutine.dto

data class SchoolResponse(
    val id: Long,
    val name: String,
    val address: String,
    val email: String,
    val students: List<StudentResponse>
)