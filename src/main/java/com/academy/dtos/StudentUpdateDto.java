package com.academy.dtos;

import com.academy.entity.University;

public class StudentUpdateDto {
    private String name;
    private String surname;
    private Integer age;
    private String email;
    private University university;
    private String password;

    public String getName() {
        return name;
    }

    public StudentUpdateDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public StudentUpdateDto setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public StudentUpdateDto setAge(Integer age) {
        this.age = age;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public StudentUpdateDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public University getUniversity() {
        return university;
    }

    public StudentUpdateDto setUniversity(University university) {
        this.university = university;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public StudentUpdateDto setPassword(String password) {
        this.password = password;
        return this;
    }
}
