package com.academy.service;

import com.academy.dtos.university.UniversityCreateDto;
import com.academy.dtos.university.UniversityUpdateDto;
import com.academy.payload.ApiResponse;

public interface UniversityService {

    ApiResponse getAllUniversities();

    ApiResponse getUniversityById(long id);

    ApiResponse createUniversity(UniversityCreateDto createDto);

    ApiResponse updateUniversity(UniversityUpdateDto updateDto, long id);

    ApiResponse deleteUniversity(long id);

    ApiResponse getTotalCount();

    ApiResponse filterUniversities(String name, String studentName, String teacherName);
}
