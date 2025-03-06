package com.academy.dtos.university;

import com.academy.entity.Student;

import java.util.List;

public class UniversityUpdateDto {
    private String name;
    private List<Student> students;

    public UniversityUpdateDto() {
    }

    public UniversityUpdateDto(String name, List<Student> students) {
        this.name = name;
        this.students = students;
    }

    public String getName() {
        return name;
    }

    public UniversityUpdateDto setName(String name) {
        this.name = name;
        return this;
    }

    public List<Student> getStudents() {
        return students;
    }

    public UniversityUpdateDto setStudents(List<Student> students) {
        this.students = students;
        return this;
    }
}
