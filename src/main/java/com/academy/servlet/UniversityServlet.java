package com.academy.servlet;

import com.academy.dtos.university.UniversityCreateDto;
import com.academy.dtos.university.UniversityUpdateDto;
import com.academy.payload.ApiResponse;
import com.academy.payload.ErrorResponse;
import com.academy.repository.impls.UniversityRepositoryImpl;
import com.academy.service.UniversityService;
import com.academy.service.impls.UniversityServiceImpl;
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

@WebServlet(urlPatterns = {"/universities", "/universities/create", "/universities/update/*",
        "/universities/delete/*", "/universities/total-count", "/universities/filter"})
public class UniversityServlet extends HttpServlet {

    private final UniversityService universityService;
    private final ObjectMapper objectMapper;
    private final org.slf4j.Logger logger;

    public UniversityServlet() {
        this.universityService = new UniversityServiceImpl(new UniversityRepositoryImpl(
                Persistence.createEntityManagerFactory("studentPU")));
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.logger = LoggerFactory.getLogger(UniversityServlet.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath(); // Esas endpoint ("/universities")
        String pathInfo = request.getPathInfo(); // Elave hisse (meselen, "/1", "/total-count")

        try {
            if ("/universities".equals(servletPath) && (pathInfo == null || pathInfo.equals("/"))) {
                // Butun universitetleri qaytar
                ApiResponse universities = universityService.getAllUniversities();
                response.getWriter().write(objectMapper.writeValueAsString(universities));
            } else if ("/total-count".equals(pathInfo)) {
                // Umumi universitet sayini qaytar
                ApiResponse count = universityService.getTotalCount();
                response.getWriter().write(objectMapper.writeValueAsString(count));
            } else if ("/filter".equals(pathInfo)) {
                // Filterlenmis universitetleri qaytar
                String name = request.getParameter("name");
                String studentName = request.getParameter("studentName");
                String teacherName = request.getParameter("teacherName");

                ApiResponse universities = universityService.filterUniversities(name, studentName, teacherName);
                response.getWriter().write(objectMapper.writeValueAsString(universities));
            } else if (pathInfo.matches("^/[0-9]+$")) {
                // ID-ye gore universiteti qaytar
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
                ApiResponse university = universityService.getUniversityById(id);
                response.getWriter().write(objectMapper.writeValueAsString(university));
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
            UniversityCreateDto createDto = objectMapper.readValue(request.getReader(), UniversityCreateDto.class);
            ApiResponse result = universityService.createUniversity(createDto);
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
            UniversityUpdateDto updateDto = objectMapper.readValue(request.getReader(), UniversityUpdateDto.class);
            ApiResponse result = universityService.updateUniversity(updateDto, id);
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
            ApiResponse result = universityService.deleteUniversity(id);
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
