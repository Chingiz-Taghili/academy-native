package com.academy.repository;

import com.academy.entity.Student;
import com.academy.entity.University;
import jakarta.persistence.*;

import java.util.*;

public class StudentRepositoryImpl implements StudentRepository {
    private final EntityManagerFactory entityManagerFactory;

    public StudentRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<Student> findAll() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<Object[]> results = entityManager.createNativeQuery("SELECT " +
                    "s.id, s.name, s.surname, s.age, s.email, s.password, u.id, u.name FROM students s " +
                    "LEFT JOIN universities u ON s.university_id = u.id").getResultList();

            List<Student> students = new ArrayList<>();
            for (Object[] row : results) {
                Student student = new Student();
                student.setId(((Number) row[0]).longValue());
                student.setName((String) row[1]);
                student.setSurname((String) row[2]);
                student.setAge(row[3] != null ? ((Number) row[3]).intValue() : null);
                student.setEmail((String) row[4]);
                student.setPassword((String) row[5]);
                // Universiteti yaratmaq
                University university = new University();
                if (row[6] != null) {  // Universitet ID null ola bilər
                    university.setId(((Number) row[6]).longValue());
                    university.setName((String) row[7]);
                }
                student.setUniversity(university);
                students.add(student);
            }
            return students;
        }
    }

    @Override
    public Optional<Student> findById(long id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {
                Object[] result = (Object[]) entityManager.createNativeQuery("SELECT " +
                        "s.id, s.name, s.surname, s.email, s.age, s.university_id, u.id, u.name FROM students s " +
                        "LEFT JOIN universities u ON s.university_id = u.id WHERE s.id = :id"
                ).setParameter("id", id).getSingleResult();

                // Student obyektini yaradırıq
                Student student = new Student();
                student.setId(((Number) result[0]).longValue());
                student.setName((String) result[1]);
                student.setSurname((String) result[2]);
                student.setEmail((String) result[3]);
                student.setAge(result[4] != null ? ((Number) result[4]).intValue() : null);
                // University obyektini bağlayırıq
                if (result[5] != null) {
                    University university = new University();
                    university.setId(((Number) result[6]).longValue());
                    university.setName((String) result[7]);
                    student.setUniversity(university);
                }
                return Optional.of(student);
            } catch (NoResultException e) {
                return Optional.empty(); //Hibernate-in "NoResultException"-ı olduqda Optional.empty() qaytarır
            }
        }
    }

    @Override
    public List<Student> findByName(String name) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<Object[]> results = entityManager.createNativeQuery("SELECT " +
                    "s.id, s.name, s.surname, s.email, s.age, s.password, u.id, u.name FROM students s " +
                    "LEFT JOIN universities u ON s.university_id = u.id WHERE s.name = :name"
            ).setParameter("name", name).getResultList();

            List<Student> students = new ArrayList<>();
            for (Object[] row : results) {
                Student student = new Student();
                student.setId(((Number) row[0]).longValue());
                student.setName((String) row[1]);
                student.setSurname((String) row[2]);
                student.setEmail((String) row[3]);
                student.setAge(row[4] != null ? ((Number) row[4]).intValue() : null);
                student.setPassword((String) row[5]);
                // Universiteti yaratmaq
                University university = null;
                if (row[6] != null) {  // Universitet ID null ola bilər
                    university = new University();
                    university.setId(((Number) row[6]).longValue());
                    university.setName((String) row[7]);
                }
                student.setUniversity(university);
                students.add(student);
            }
            return students;
        }
    }

    @Override
    public void save(Student student) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                if (student.getId() == null) {
                    //id olmadiqda servere yeni Student elave edir
                    entityManager.createNativeQuery("INSERT INTO students (name, surname, " +
                                    "age, email, university_id, password) VALUES (?, ?, ?, ?, ?, ?)")
                            .setParameter(1, student.getName())
                            .setParameter(2, student.getSurname())
                            .setParameter(3, student.getAge())
                            .setParameter(4, student.getEmail())
                            .setParameter(5, student.getUniversity().getId())
                            .setParameter(6, student.getPassword())
                            .executeUpdate();
                } else {
                    //serverde id-ye uygun Student tapildiqda onu yenileyir
                    int updatedRows = entityManager.createNativeQuery("UPDATE students SET name = ?, " +
                                    "surname = ?, age = ?, email = ?, university_id = ?, password = ? WHERE id = ?")
                            .setParameter(1, student.getName())
                            .setParameter(2, student.getSurname())
                            .setParameter(3, student.getAge())
                            .setParameter(4, student.getEmail())
                            .setParameter(5, student.getUniversity().getId())
                            .setParameter(6, student.getPassword())
                            .setParameter(7, student.getId())
                            .executeUpdate();
                    if (updatedRows == 0) {
                        throw new NoResultException("Student with id " + student.getId() + " not found");
                    }
                }
                transaction.commit();
            } catch (PersistenceException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new PersistenceException("Error saving student", e);
            }
        }
    }

    @Override
    public void delete(Student student) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            final EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                if (student != null && student.getId() != null) {
                    int deletedRows = entityManager.createNativeQuery("DELETE FROM students WHERE id = ?")
                            .setParameter(1, student.getId()).executeUpdate();
                    if (deletedRows == 0) {
                        throw new NoResultException("Student with id " + student.getId() + " not found");
                    }
                }
                transaction.commit();
            } catch (PersistenceException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new PersistenceException("Error deleting student", e);
            }
        }
    }

    @Override
    public void deleteById(long id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            final EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                int deletedRows = entityManager.createNativeQuery(
                        "DELETE FROM students WHERE id = :id").setParameter("id", id).executeUpdate();
                if (deletedRows == 0) {
                    throw new NoResultException("Student with id " + id + " not found");
                }
                transaction.commit();
            } catch (PersistenceException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new PersistenceException("Error deleting student with id: " + id, e);
            }
        }
    }

    @Override
    public long count() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return ((Number) entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM students").getSingleResult()).longValue();
        } catch (PersistenceException e) {
            throw new PersistenceException("Error counting students", e);
        }
    }

    @Override
    public List<Student> search(String name, String surname, String email, String universityName, Integer age) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT s.id, s.name, s.surname, s.email, s.age, " +
                            "u.id as university_id, u.name as university_name FROM students s ");
            sql.append("LEFT JOIN universities u ON s.university_id = u.id WHERE 1=1 ");
            Map<String, Object> parameters = new HashMap<>();
            if (name != null && !name.isEmpty()) {
                sql.append("AND LOWER(s.name) LIKE :name ");
                parameters.put("name", "%" + name.toLowerCase() + "%");
            }
            if (surname != null && !surname.isEmpty()) {
                sql.append("AND LOWER(s.surname) LIKE :surname ");
                parameters.put("surname", "%" + surname.toLowerCase() + "%");
            }
            if (email != null && !email.isEmpty()) {
                sql.append("AND LOWER(s.email) LIKE :email ");
                parameters.put("email", "%" + email.toLowerCase() + "%");
            }
            if (universityName != null && !universityName.isEmpty()) {
                sql.append("AND LOWER(u.name) LIKE :universityName ");
                parameters.put("universityName", "%" + universityName.toLowerCase() + "%");
            }
            if (age != null) {
                sql.append("AND s.age = :age ");
                parameters.put("age", age);
            }
            Query query = entityManager.createNativeQuery(sql.toString());
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            List<Object[]> results = query.getResultList();
            List<Student> students = new ArrayList<>();
            for (Object[] row : results) {
                Student student = new Student();
                student.setId(((Number) row[0]).longValue());
                student.setName((String) row[1]);
                student.setSurname((String) row[2]);
                student.setEmail((String) row[3]);
                student.setAge((row[4] != null) ? ((Number) row[4]).intValue() : null);

                University university = null;
                if (row[5] != null) {
                    university = new University();
                    university.setId(((Number) row[5]).longValue());
                    university.setName((String) row[6]);
                }
                student.setUniversity(university);
                students.add(student);
            }
            return students;
        }
    }
}
