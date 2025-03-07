package com.academy.dtos.teacher;

import com.academy.entity.University;

public class TeacherUpdateDto {
    private String name;
    private String surname;
    private Integer age;
    private String email;
    private University university;
    private String password;

    public String getName() {
        return name;
    }

    public TeacherUpdateDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public TeacherUpdateDto setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public TeacherUpdateDto setAge(Integer age) {
        this.age = age;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public TeacherUpdateDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public University getUniversity() {
        return university;
    }

    public TeacherUpdateDto setUniversity(University university) {
        this.university = university;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public TeacherUpdateDto setPassword(String password) {
        this.password = password;
        return this;
    }
}
