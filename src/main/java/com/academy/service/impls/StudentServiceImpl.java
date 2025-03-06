package com.academy.service.impls;

import com.academy.entity.Student;
import com.academy.payload.ApiResponse;
import com.academy.repository.StudentRepository;
import com.academy.service.StudentService;
import com.academy.util.StudentMapper;
import com.academy.dtos.student.StudentCreateDto;
import com.academy.dtos.student.StudentDto;
import com.academy.dtos.student.StudentUpdateDto;
import com.academy.payload.DataResponse;
import com.academy.payload.MessageResponse;
import jakarta.persistence.NoResultException;

import java.util.List;

public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public ApiResponse getAllStudents() {
        List<Student> findStudents = studentRepository.findAll();
        if (findStudents.isEmpty()) {
            return new MessageResponse("No students available");
        }
        List<StudentDto> students = findStudents.stream().map(StudentMapper.INSTANCE::toDto).toList();
        return new DataResponse<>(students);
    }

    @Override
    public ApiResponse getStudentById(long id) {
        Student findStudent = studentRepository.findById(id).orElseThrow(
                () -> new NoResultException("Student not found with id: " + id));
        StudentDto student = StudentMapper.INSTANCE.toDto(findStudent);
        return new DataResponse<>(student);
    }

    @Override
    public ApiResponse createStudent(StudentCreateDto createDto) {
        Student newStudent = StudentMapper.INSTANCE.toEntity(createDto);
        studentRepository.save(newStudent);
        return new MessageResponse("Student created successfully");
    }

    @Override
    public ApiResponse updateStudent(StudentUpdateDto updateDto, long id) {
        Student findStudent = studentRepository.findById(id).orElseThrow(
                () -> new NoResultException("Student not found with id: " + id));
        findStudent.setName(updateDto.getName()).setSurname(updateDto.getSurname())
                .setAge(updateDto.getAge()).setEmail(updateDto.getEmail())
                .setUniversity(updateDto.getUniversity()).setPassword(updateDto.getPassword());
        studentRepository.save(findStudent);
        return new MessageResponse("Student updated successfully");
    }

    @Override
    public ApiResponse deleteStudent(long id) {
        studentRepository.findById(id).orElseThrow(
                () -> new NoResultException("Student not found with id: " + id));
        studentRepository.deleteById(id);
        return new MessageResponse("Student deleted successfully");
    }

    @Override
    public ApiResponse getTotalCount() {
        return new DataResponse<>(studentRepository.count());
    }

    @Override
    public ApiResponse filterStudents(String name, String surname, String email, String university, Integer age) {
        List<Student> findStudents = studentRepository.filter(name, surname, email, university, age);
        List<StudentDto> students = findStudents.stream().map(StudentMapper.INSTANCE::toDto).toList();
        return new DataResponse<>(students);
    }
}
