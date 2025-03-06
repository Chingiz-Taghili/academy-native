package com.academy.servlet;

import com.academy.dtos.StudentCreateDto;
import com.academy.dtos.StudentUpdateDto;
import com.academy.payload.ApiResponse;
import com.academy.payload.ErrorResponse;
import com.academy.repository.StudentRepositoryImpl;
import com.academy.service.StudentService;
import com.academy.service.StudentServiceImpl;
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

@WebServlet(urlPatterns = {"/students", "/students/create", "/students/update/*",
        "/students/delete/*", "/students/total-count", "/students/filter"})
public class StudentServlet extends HttpServlet {

    private final StudentService studentService;
    private final ObjectMapper objectMapper;
    private final org.slf4j.Logger logger;

    public StudentServlet() {
        this.studentService = new StudentServiceImpl(new StudentRepositoryImpl(
                Persistence.createEntityManagerFactory("studentPU")));
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.logger = LoggerFactory.getLogger(StudentServlet.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath(); // Esas endpoint ("/students")
        String pathInfo = request.getPathInfo(); // Elave hisse (meselen, "/1", "/total-count")

        try {
            if ("/students".equals(servletPath) && (pathInfo == null || pathInfo.equals("/"))) {
                // Butun telebeleri qaytar
                ApiResponse students = studentService.getAllStudents();
                response.getWriter().write(objectMapper.writeValueAsString(students));
            } else if ("/total-count".equals(pathInfo)) {
                // Umumi telebe sayini qaytar
                ApiResponse count = studentService.getTotalCount();
                response.getWriter().write(objectMapper.writeValueAsString(count));
            } else if ("/filter".equals(pathInfo)) {
                // Filterlenmis telebeleri qaytar
                String name = request.getParameter("name");
                String surname = request.getParameter("surname");
                String email = request.getParameter("email");
                String university = request.getParameter("university");
                Integer age = request.getParameter("age") != null ? Integer.parseInt(request.getParameter("age")) : null;

                ApiResponse students = studentService.filterStudents(name, surname, email, university, age);
                response.getWriter().write(objectMapper.writeValueAsString(students));
            } else if (pathInfo.matches("^/[0-9]+$")) {
                // ID-ye gore telebeni qaytar
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
                ApiResponse student = studentService.getStudentById(id);
                response.getWriter().write(objectMapper.writeValueAsString(student));
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
            StudentCreateDto createDto = objectMapper.readValue(request.getReader(), StudentCreateDto.class);
            ApiResponse result = studentService.createStudent(createDto);
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
            StudentUpdateDto updateDto = objectMapper.readValue(request.getReader(), StudentUpdateDto.class);
            ApiResponse apiResponse = studentService.updateStudent(updateDto, id);
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
            ApiResponse result = studentService.deleteStudent(id);
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
