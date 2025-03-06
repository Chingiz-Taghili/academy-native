package com.academy.util;

import com.academy.dtos.university.UniversityCreateDto;
import com.academy.dtos.university.UniversityDto;
import com.academy.dtos.university.UniversityUpdateDto;
import com.academy.entity.University;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UniversityMapper {
    UniversityMapper INSTANCE = Mappers.getMapper(UniversityMapper.class);

//    @Mapping(source = "surname", target = "lastname")
    UniversityDto toDto(University university);
//    @Mapping(target = "id", ignore = true)
    University toEntity(UniversityCreateDto createDto);
    University toEntity(UniversityUpdateDto updateDto);
}