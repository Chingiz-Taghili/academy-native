package com.academy.service;

import com.academy.dtos.teacher.TeacherCreateDto;
import com.academy.dtos.teacher.TeacherUpdateDto;
import com.academy.payload.ApiResponse;

public interface TeacherService {

    ApiResponse getAllTeachers();

    ApiResponse getTeacherById(long id);

    ApiResponse createTeacher(TeacherCreateDto createDto);

    ApiResponse updateTeacher(TeacherUpdateDto updateDto, long id);

    ApiResponse deleteTeacher(long id);

    ApiResponse getTotalCount();

    ApiResponse filterTeachers(String name, String surname, String email, String universityName, Integer age);
}
