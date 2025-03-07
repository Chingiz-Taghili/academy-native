package com.academy.servlet;

import com.academy.dtos.teacher.TeacherCreateDto;
import com.academy.dtos.teacher.TeacherUpdateDto;
import com.academy.payload.ApiResponse;
import com.academy.payload.ErrorResponse;
import com.academy.repository.impls.TeacherRepositoryImpl;
import com.academy.service.TeacherService;
import com.academy.service.impls.TeacherServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(urlPatterns = {"/teachers", "/teachers/create", "/teachers/update/*",
        "/teachers/delete/*", "/teachers/total-count", "/teachers/filter"})
public class TeacherServlet extends HttpServlet {

    private final TeacherService teacherService;
    private final ObjectMapper objectMapper;
    private final org.slf4j.Logger logger;

    public TeacherServlet() {
        this.teacherService = new TeacherServiceImpl(new TeacherRepositoryImpl(
                Persistence.createEntityManagerFactory("studentPU")));
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.logger = LoggerFactory.getLogger(TeacherServlet.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath(); // Esas endpoint ("/teachers")
        String pathInfo = request.getPathInfo(); // Elave hisse (meselen, "/1", "/total-count")

        try {
            if ("/teachers".equals(servletPath) && (pathInfo == null || pathInfo.equals("/"))) {
                // Butun muellimleri qaytar
                ApiResponse teachers = teacherService.getAllTeachers();
                response.getWriter().write(objectMapper.writeValueAsString(teachers));
            } else if ("/total-count".equals(pathInfo)) {
                // Umumi muellim sayini qaytar
                ApiResponse count = teacherService.getTotalCount();
                response.getWriter().write(objectMapper.writeValueAsString(count));
            } else if ("/filter".equals(pathInfo)) {
                // Filterlenmis muellimleri qaytar
                String name = request.getParameter("name");
                String surname = request.getParameter("surname");
                String email = request.getParameter("email");
                String university = request.getParameter("university");
                Integer age = request.getParameter("age") != null ? Integer.parseInt(request.getParameter("age")) : null;

                ApiResponse teachers = teacherService.filterTeachers(name, surname, email, university, age);
                response.getWriter().write(objectMapper.writeValueAsString(teachers));
            } else if (pathInfo.matches("^/[0-9]+$")) {
                // ID-ye gore muellimi qaytar
                String idStr = pathInfo.substring(1); // "/1" -> "1"
                if (!idStr.matches("\\d+")) { //Eded olub-olmadigi yoxlanilir
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                            "Invalid ID format. ID must be a number.", HttpServletResponse.SC_BAD_REQUEST)));
                    return;
                }
                long id = Long.parseLong(idStr);
                if (id <= 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                            "ID must be a positive number!", HttpServletResponse.SC_BAD_REQUEST)));
                    return;
                }
                ApiResponse teacher = teacherService.getTeacherById(id);
                response.getWriter().write(objectMapper.writeValueAsString(teacher));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                        "Invalid request!", HttpServletResponse.SC_BAD_REQUEST)));
            }
        } catch (NumberFormatException e) {
            logger.error("Exception occurred: ", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                    "ID must be a number!", HttpServletResponse.SC_BAD_REQUEST)));
        } catch (NoResultException e) {
            logger.error("Exception occurred: ", e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ErrorResponse(e.getMessage(), HttpServletResponse.SC_NOT_FOUND)));
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                    "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.equals("/create")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                    "Invalid request!", HttpServletResponse.SC_BAD_REQUEST)));
            return;
        }
        try {
            TeacherCreateDto createDto = objectMapper.readValue(request.getReader(), TeacherCreateDto.class);
            ApiResponse result = teacherService.createTeacher(createDto);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                    "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR)));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/update/") || pathInfo.length() <= 8) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                    "Invalid request! The URL is incorrect or the ID is missing.", HttpServletResponse.SC_BAD_REQUEST)));
            return;
        }
        try {
            long id = Long.parseLong(pathInfo.substring(8)); // "/update/" çıxarılır
            if (id <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                        "ID must be a positive number!", HttpServletResponse.SC_BAD_REQUEST)));
                return;
            }
            TeacherUpdateDto updateDto = objectMapper.readValue(request.getReader(), TeacherUpdateDto.class);
            ApiResponse apiResponse = teacherService.updateTeacher(updateDto, id);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        } catch (NumberFormatException e) {
            logger.error("Exception occurred: ", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                    "ID must be a number!", HttpServletResponse.SC_BAD_REQUEST)));
        } catch (NoResultException e) {
            logger.error("Exception occurred: ", e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ErrorResponse(e.getMessage(), HttpServletResponse.SC_NOT_FOUND)));
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                    "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR)));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/delete/") || pathInfo.length() <= 8) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                    "Invalid request! The URL is incorrect or the ID is missing.", HttpServletResponse.SC_BAD_REQUEST)));
            return;
        }
        try {
            long id = Long.parseLong(pathInfo.substring(8)); // "/delete/" çıxarılır
            if (id <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                        "ID must be a positive number!", HttpServletResponse.SC_BAD_REQUEST)));
                return;
            }
            ApiResponse result = teacherService.deleteTeacher(id);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (NumberFormatException e) {
            logger.error("Exception occurred: ", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                    "ID must be a number!", HttpServletResponse.SC_BAD_REQUEST)));
        } catch (NoResultException e) {
            logger.error("Exception occurred: ", e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ErrorResponse(e.getMessage(), HttpServletResponse.SC_NOT_FOUND)));
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                    "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR)));
        }
    }
}
