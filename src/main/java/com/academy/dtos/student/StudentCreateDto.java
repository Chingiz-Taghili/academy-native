package com.academy.dtos.student;

import com.academy.entity.University;

public class StudentCreateDto {
    private String name;
    private String surname;
    private Integer age;
    private String email;
    private University university;
    private String password;

    public String getName() {
        return name;
    }

    public StudentCreateDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public StudentCreateDto setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public StudentCreateDto setAge(Integer age) {
        this.age = age;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public StudentCreateDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public University getUniversity() {
        return university;
    }

    public StudentCreateDto setUniversity(University university) {
        this.university = university;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public StudentCreateDto setPassword(String password) {
        this.password = password;
        return this;
    }
}
