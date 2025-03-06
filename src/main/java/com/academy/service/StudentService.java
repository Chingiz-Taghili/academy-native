package com.academy.service;

import com.academy.dtos.StudentCreateDto;
import com.academy.dtos.StudentUpdateDto;
import com.academy.payload.ApiResponse;

public interface StudentService {

    ApiResponse getAllStudents();

    ApiResponse getStudentById(long id);

    ApiResponse createStudent(StudentCreateDto createDto);

    ApiResponse updateStudent(StudentUpdateDto updateDto, long id);

    ApiResponse deleteStudent(long id);

    ApiResponse getTotalCount();

    ApiResponse filterStudents(String name, String surname, String email, String university, Integer age);
}
