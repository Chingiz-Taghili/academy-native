package com.academy.repository;

import com.academy.entity.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {

    List<Student> findAll();

    Optional<Student> findById(long id);

    List<Student> findByName(String name);

    void save(Student student);

    void delete(Student student);

    void deleteById(long id);

    long count();

    List<Student> filter(String name, String surname, String email, String universityName, Integer age);
}