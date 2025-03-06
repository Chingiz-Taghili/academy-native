package com.academy.repository;


import com.academy.entity.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcRepository {
    public Connection connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/education-jpa?user=postgres&password=12345&characterEncoding=UTF-8");
    }

    public List<Student> findAll() {
        List<Student> result = new ArrayList<>();
        try (Connection connection = connect()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from students");
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String surname = resultSet.getString("surname");
                String email = resultSet.getString("email");
                int age = resultSet.getInt("age");

                Student student = new Student().setId(id).setName(name).setSurname(surname).setEmail(email).setAge(age);
                result.add(student);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Student findById(long id) {
        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement(
                    "select * from students where id=?");
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Student student = new Student();
                student.setId(resultSet.getLong("id"));
                student.setName(resultSet.getString("name"));
                student.setSurname(resultSet.getString("surname"));
                student.setAge(resultSet.getInt("age"));
                return student;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Student> search(String name, String surname) {
        List<Student> result = new ArrayList<>();
        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement(
                    "select * from students where name=? or surname=?");
            statement.setString(1, name);
            statement.setString(2, surname);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Student student = new Student();
                student.setId(resultSet.getLong("id"));
                student.setName(resultSet.getString("name"));
                student.setSurname(resultSet.getString("surname"));
                student.setAge(resultSet.getInt("age"));
                result.add(student);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void insert(Student student) {
        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement(
                    "insert into students(name, surname, age) values(?, ?, ?)");
            statement.setString(1, student.getName());
            statement.setString(2, student.getSurname());
            statement.setInt(3, student.getAge());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(Student student) {
        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement(
                    "update students set name=?, surname=?, age=? where id=?");
            statement.setString(1, student.getName());
            statement.setString(2, student.getSurname());
            statement.setInt(3, student.getAge());
            statement.setLong(4, student.getId());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(Student student) {
        try (Connection connection = connect()) {
            PreparedStatement statement = connection.prepareStatement("delete from students where id=?");
            statement.setLong(1, student.getId());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
