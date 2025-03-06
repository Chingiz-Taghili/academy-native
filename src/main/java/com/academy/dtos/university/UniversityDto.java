package com.academy.dtos.university;

import com.academy.entity.Student;

import java.util.List;

public class UniversityDto {
    private Long id;
    private String name;
    private List<Student> students;

    public UniversityDto() {
    }

    public UniversityDto(Long id, String name, List<Student> students) {
        this.id = id;
        this.name = name;
        this.students = students;
    }

    public Long getId() {
        return id;
    }

    public UniversityDto setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public UniversityDto setName(String name) {
        this.name = name;
        return this;
    }

    public List<Student> getStudents() {
        return students;
    }

    public UniversityDto setStudents(List<Student> students) {
        this.students = students;
        return this;
    }
}
