package com.academy.service.impls;

import com.academy.dtos.teacher.TeacherCreateDto;
import com.academy.dtos.teacher.TeacherDto;
import com.academy.dtos.teacher.TeacherUpdateDto;
import com.academy.entity.Teacher;
import com.academy.payload.ApiResponse;
import com.academy.payload.DataResponse;
import com.academy.payload.MessageResponse;
import com.academy.repository.TeacherRepository;
import com.academy.service.TeacherService;
import com.academy.util.TeacherMapper;
import jakarta.persistence.NoResultException;

import java.util.List;

public class TeacherServiceImpl implements TeacherService {
    private final TeacherRepository teacherRepository;

    public TeacherServiceImpl(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public ApiResponse getAllTeachers() {
        List<Teacher> findTeachers = teacherRepository.findAll();
        if (findTeachers.isEmpty()) {
            return new MessageResponse("No teachers available");
        }
        List<TeacherDto> teachers = findTeachers.stream().map(TeacherMapper.INSTANCE::toDto).toList();
        return new DataResponse<>(teachers);
    }

    @Override
    public ApiResponse getTeacherById(long id) {
        Teacher findTeacher = teacherRepository.findById(id).orElseThrow(
                () -> new NoResultException("Teacher not found with id: " + id));
        TeacherDto teacher = TeacherMapper.INSTANCE.toDto(findTeacher);
        return new DataResponse<>(teacher);
    }

    @Override
    public ApiResponse createTeacher(TeacherCreateDto createDto) {
        Teacher newTeacher = TeacherMapper.INSTANCE.toEntity(createDto);
        teacherRepository.save(newTeacher);
        return new MessageResponse("Teacher created successfully");
    }

    @Override
    public ApiResponse updateTeacher(TeacherUpdateDto updateDto, long id) {
        Teacher findTeacher = teacherRepository.findById(id).orElseThrow(
                () -> new NoResultException("Teacher not found with id: " + id));
        findTeacher.setName(updateDto.getName()).setSurname(updateDto.getSurname())
                .setAge(updateDto.getAge()).setEmail(updateDto.getEmail())
                .setUniversity(updateDto.getUniversity()).setPassword(updateDto.getPassword());
        teacherRepository.save(findTeacher);
        return new MessageResponse("Teacher updated successfully");
    }

    @Override
    public ApiResponse deleteTeacher(long id) {
        teacherRepository.findById(id).orElseThrow(
                () -> new NoResultException("Teacher not found with id: " + id));
        teacherRepository.deleteById(id);
        return new MessageResponse("Teacher deleted successfully");
    }

    @Override
    public ApiResponse getTotalCount() {
        return new DataResponse<>(teacherRepository.count());
    }

    @Override
    public ApiResponse filterTeachers(String name, String surname, String email, String universityName, Integer age) {
        List<Teacher> findTeachers = teacherRepository.filter(name, surname, email, universityName, age);
        List<TeacherDto> teachers = findTeachers.stream().map(TeacherMapper.INSTANCE::toDto).toList();
        return new DataResponse<>(teachers);
    }
}
