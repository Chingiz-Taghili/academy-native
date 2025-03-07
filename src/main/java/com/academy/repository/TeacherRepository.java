package com.academy.repository;

import com.academy.entity.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository {

    List<Teacher> findAll();

    Optional<Teacher> findById(long id);

    List<Teacher> findByName(String name);

    void save(Teacher teacher);

    void delete(Teacher teacher);

    void deleteById(long id);

    long count();

    List<Teacher> filter(String name, String surname, String email, String universityName, Integer age);
}