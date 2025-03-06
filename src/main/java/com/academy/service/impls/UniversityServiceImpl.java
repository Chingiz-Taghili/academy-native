package com.academy.service.impls;

import com.academy.dtos.university.UniversityCreateDto;
import com.academy.dtos.university.UniversityDto;
import com.academy.dtos.university.UniversityUpdateDto;
import com.academy.entity.University;
import com.academy.payload.ApiResponse;
import com.academy.payload.DataResponse;
import com.academy.payload.MessageResponse;
import com.academy.repository.UniversityRepository;
import com.academy.service.UniversityService;
import com.academy.util.UniversityMapper;
import jakarta.persistence.NoResultException;

import java.util.List;

public class UniversityServiceImpl implements UniversityService {
    private final UniversityRepository universityRepository;

    public UniversityServiceImpl(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    @Override
    public ApiResponse getAllUniversities() {
        List<University> findUniversities = universityRepository.findAll();
        if (findUniversities.isEmpty()) {
            return new MessageResponse("No universities available");
        }
        List<UniversityDto> universities = findUniversities.stream().map(UniversityMapper.INSTANCE::toDto).toList();
        return new DataResponse<>(universities);
    }

    @Override
    public ApiResponse getUniversityById(long id) {
        University findUniversity = universityRepository.findById(id).orElseThrow(
                () -> new NoResultException("University not found with id: " + id));
        UniversityDto university = UniversityMapper.INSTANCE.toDto(findUniversity);
        return new DataResponse<>(university);
    }

    @Override
    public ApiResponse createUniversity(UniversityCreateDto createDto) {
        University newUniversity = UniversityMapper.INSTANCE.toEntity(createDto);
        universityRepository.save(newUniversity);
        return new MessageResponse("University created successfully");
    }

    @Override
    public ApiResponse updateUniversity(UniversityUpdateDto updateDto, long id) {
        University findUniversity = universityRepository.findById(id).orElseThrow(
                () -> new NoResultException("University not found with id: " + id));
        findUniversity.setName(updateDto.getName()).setStudents(updateDto.getStudents());
        universityRepository.save(findUniversity);
        return new MessageResponse("University updated successfully");
    }

    @Override
    public ApiResponse deleteUniversity(long id) {
        universityRepository.findById(id).orElseThrow(
                () -> new NoResultException("University not found with id: " + id));
        universityRepository.deleteById(id);
        return new MessageResponse("University deleted successfully");
    }

    @Override
    public ApiResponse getTotalCount() {
        return new DataResponse<>(universityRepository.count());
    }

    @Override
    public ApiResponse filterUniversities(String name, String studentName, String teacherName) {
        List<University> findUniversities = universityRepository.filter(name, studentName, teacherName);
        List<UniversityDto> universities = findUniversities.stream().map(UniversityMapper.INSTANCE::toDto).toList();
        return new DataResponse<>(universities);
    }
}
