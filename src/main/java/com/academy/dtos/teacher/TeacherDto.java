package com.academy.dtos.teacher;


import com.academy.entity.University;

public class TeacherDto {
    private Long id;
    private String name;
    private String surname;
    private Integer age;
    private String email;
    private University university;
    private String password;

    public Long getId() {
        return id;
    }

    public TeacherDto setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TeacherDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public TeacherDto setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public TeacherDto setAge(Integer age) {
        this.age = age;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public TeacherDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public University getUniversity() {
        return university;
    }

    public TeacherDto setUniversity(University university) {
        this.university = university;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public TeacherDto setPassword(String password) {
        this.password = password;
        return this;
    }
}
