package com.academy.util;

import com.academy.dtos.teacher.TeacherCreateDto;
import com.academy.dtos.teacher.TeacherDto;
import com.academy.dtos.teacher.TeacherUpdateDto;
import com.academy.entity.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TeacherMapper {
    TeacherMapper INSTANCE = Mappers.getMapper(TeacherMapper.class);

    //    @Mapping(source = "surname", target = "lastname")
    TeacherDto toDto(Teacher teacher);

    //    @Mapping(target = "id", ignore = true)
    Teacher toEntity(TeacherCreateDto createDto);

    Teacher toEntity(TeacherUpdateDto updateDto);
}