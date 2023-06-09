package com.geovannycode.apirestcoroutine.controller

import com.geovannycode.apirestcoroutine.dto.SchoolRequest
import com.geovannycode.apirestcoroutine.dto.SchoolResponse
import com.geovannycode.apirestcoroutine.model.School
import com.geovannycode.apirestcoroutine.model.Student
import com.geovannycode.apirestcoroutine.service.SchoolService
import com.geovannycode.apirestcoroutine.service.StudentService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/schools")
class SchoolController(
    private val schoolService: SchoolService,
    private val studentService: StudentService,
) {
    @GetMapping
    suspend fun findSchool(
        @RequestParam("name", required = false) name: String?,
    ): Flow<SchoolResponse> {
        val schools = name?.let { schoolService.findAllSchoolsByNameLike(name) }
            ?: schoolService.findAllSchools()

        return schools
            .map { school ->
                school.toResponse(
                    students = findSchoolStudents(school),
                )
            }
    }

    @PostMapping
    suspend fun createSchool(@RequestBody schoolRequest: SchoolRequest): SchoolResponse =
        schoolService.saveSchool(
            school = schoolRequest.toModel(),
        )?.toResponse()
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during school creation.")

    @GetMapping("/{id}")
    suspend fun findSchoolById(
        @PathVariable id: Long,
    ): SchoolResponse =
        schoolService.findSchoolById(id)
            ?.let { school ->
                school.toResponse(
                    students = findSchoolStudents(school),
                )
            }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "School with id $id not found.")

    @PutMapping("/{id}")
    suspend fun updateSchool(
        @PathVariable id: Long,
        @RequestBody schoolRequest: SchoolRequest
    ): SchoolResponse =
        schoolService.updateSchool(
            id = id,
            requestedSchool = schoolRequest.toModel()
        )
            .let { school ->
                school.toResponse(
                    students = findSchoolStudents(school)
                )
            }

    @DeleteMapping("/{id}")
    suspend fun deleteSchoolById(
        @PathVariable id: Long
    ) {
        schoolService.deleteSchoolById(id)
    }

    private suspend fun findSchoolStudents(school: School) =
        studentService.findStudentsBySchoolId(school.id!!)
            .toList()
}
private fun School.toResponse(students: List<Student> = emptyList()): SchoolResponse =
    SchoolResponse(
        id = this.id!!,
        name = this.name,
        address = this.address,
        email = this.email,
        students = students.map(Student::toResponse),
    )

private fun SchoolRequest.toModel(): School =
    School(
        name = this.name,
        address = this.address,
        email = this.email,
    )
