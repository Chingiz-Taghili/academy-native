package com.academy.repository;

import com.academy.entity.University;

import java.util.List;
import java.util.Optional;

public interface UniversityRepository {

    List<University> findAll();

    Optional<University> findById(long id);

    List<University> findByName(String name);

    void save(University university);

    void delete(University university);

    void deleteById(long id);

    long count();

    List<University> filter(String name, String studentName, String teacherName);
}