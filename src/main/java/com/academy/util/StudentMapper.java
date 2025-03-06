package com.academy.util;

import com.academy.dtos.student.StudentCreateDto;
import com.academy.dtos.student.StudentDto;
import com.academy.dtos.student.StudentUpdateDto;
import com.academy.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StudentMapper {
    StudentMapper INSTANCE = Mappers.getMapper(StudentMapper.class);

//    @Mapping(source = "surname", target = "lastname")
    StudentDto toDto(Student student);
//    @Mapping(target = "id", ignore = true)
    Student toEntity(StudentCreateDto createDto);
    Student toEntity(StudentUpdateDto updateDto);
}