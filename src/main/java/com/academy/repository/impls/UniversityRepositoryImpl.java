package com.academy.repository.impls;

import com.academy.entity.Student;
import com.academy.entity.University;
import com.academy.repository.UniversityRepository;
import jakarta.persistence.*;

import java.util.*;

public class UniversityRepositoryImpl implements UniversityRepository {
    private final EntityManagerFactory entityManagerFactory;

    public UniversityRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<University> findAll() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<Object[]> results = entityManager.createNativeQuery("SELECT " +
                    "u.id, u.name, s.id, s.name, s.surname, s.age, s.email, s.password FROM universities u " +
                    "LEFT JOIN students s ON u.id = s.university_id").getResultList();

            Map<Long, University> universityMap = new HashMap<>();

            for (Object[] row : results) {
                Long universityId = ((Number) row[0]).longValue();
                String universityName = (String) row[1];

                University university = universityMap.computeIfAbsent(universityId, id -> {
                    University u = new University();
                    u.setId(id);
                    u.setName(universityName);
                    u.setStudents(new ArrayList<>());
                    return u;
                });

                if (row[2] != null) {
                    Student student = new Student();
                    student.setId(((Number) row[2]).longValue());
                    student.setName((String) row[3]);
                    student.setSurname((String) row[4]);
                    student.setAge(row[5] != null ? ((Number) row[5]).intValue() : null);
                    student.setEmail((String) row[6]);
                    student.setPassword((String) row[7]);
                    student.setUniversity(university);

                    university.getStudents().add(student);
                }
            }
            return new ArrayList<>(universityMap.values());
        }
    }

    @Override
    public Optional<University> findById(long id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {
                List<Object[]> results = (List<Object[]>) entityManager.createNativeQuery("SELECT " +
                        "u.id, u.name, s.id, s.name, s.surname, s.email, s.age FROM universities u " +
                        "LEFT JOIN students s ON s.university_id = u.id WHERE u.id = :id"
                ).setParameter("id", id).getResultList();

                // University obyektini yaradırıq
                University university = new University();
                university.setId(((Number) results.get(0)[0]).longValue());
                university.setName((String) results.get(0)[1]);
                university.setStudents(new ArrayList<>());

                // Student obyektini bağlayırıq
                for (Object[] row : results) {
                    if (row[2] != null) {
                        Student student = new Student();
                        student.setId(((Number) row[2]).longValue());
                        student.setName((String) row[3]);
                        student.setSurname((String) row[4]);
                        student.setEmail((String) row[5]);
                        student.setAge(row[6] != null ? ((Number) row[6]).intValue() : null);
                        student.setUniversity(university);

                        university.getStudents().add(student);
                    }
                }
                return Optional.of(university);
            } catch (NoResultException e) {
                return Optional.empty(); //Hibernate-in "NoResultException"-ı olduqda Optional.empty() qaytarır
            }
        }
    }

    @Override
    public List<University> findByName(String name) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<Object[]> results = entityManager.createNativeQuery("SELECT " +
                            "u.id, u.name, s.id, s.name, s.surname, s.email, s.age, s.password FROM universities u " +
                            "LEFT JOIN students s ON s.university_id = u.id WHERE u.name = :name")
                    .setParameter("name", name).getResultList();

            Map<Long, University> universityMap = new HashMap<>();

            for (Object[] row : results) {
                Long universityId = ((Number) row[0]).longValue();

                University university = universityMap.get(universityId);
                if (university == null) {
                    university = new University();
                    university.setId(universityId);
                    university.setName((String) row[1]);
                    university.setStudents(new ArrayList<>());
                    universityMap.put(universityId, university);
                }
                if (row[2] != null) {
                    Student student = new Student();
                    student.setId(((Number) row[2]).longValue());
                    student.setName((String) row[3]);
                    student.setSurname((String) row[4]);
                    student.setEmail((String) row[5]);
                    student.setAge(row[6] != null ? ((Number) row[6]).intValue() : null);
                    student.setPassword((String) row[7]);
                    student.setUniversity(university);

                    university.getStudents().add(student);
                }
            }
            return new ArrayList<>(universityMap.values());
        }
    }

    @Override
    public void save(University university) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                if (university.getId() == null) {
                    // ID olmadıqda yeni University əlavə edir və ID-ni geri qaytarır
                    Long generatedId = ((Number) entityManager.createNativeQuery(
                                    "INSERT INTO universities (name) VALUES (?) RETURNING id")
                            .setParameter(1, university.getName())
                            .getSingleResult()).longValue();
                    university.setId(generatedId);
                } else {
                    // ID olduqda mövcud University-ni yeniləyir
                    int updatedRows = entityManager.createNativeQuery("UPDATE universities SET name = ? WHERE id = ?")
                            .setParameter(1, university.getName())
                            .setParameter(2, university.getId())
                            .executeUpdate();
                    if (updatedRows == 0) {
                        throw new NoResultException("University with id " + university.getId() + " not found");
                    }
                }

                // Universitetin tələbələrini yadda saxlayır:
                if (university.getStudents() != null && !university.getStudents().isEmpty()) {
                    for (Student student : university.getStudents()) {
                        if (student.getId() == null) {
                            // ID olmadıqda yeni Student əlavə edir və ID-ni geri qaytarır
                            Long studentId = ((Number) entityManager.createNativeQuery(
                                            "INSERT INTO students (name, surname, age, email, university_id, password) " +
                                                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING id")
                                    .setParameter(1, student.getName())
                                    .setParameter(2, student.getSurname())
                                    .setParameter(3, student.getAge())
                                    .setParameter(4, student.getEmail())
                                    .setParameter(5, university.getId())
                                    .setParameter(6, student.getPassword())
                                    .getSingleResult()).longValue();
                            student.setId(studentId);
                        } else {
                            // ID olduqda mövcud Student-i yeniləyir
                            int updatedRows = entityManager.createNativeQuery("UPDATE students SET name = ?, surname = ?, age = ?, email = ?, university_id = ?, password = ? WHERE id = ?")
                                    .setParameter(1, student.getName())
                                    .setParameter(2, student.getSurname())
                                    .setParameter(3, student.getAge())
                                    .setParameter(4, student.getEmail())
                                    .setParameter(5, university.getId())
                                    .setParameter(6, student.getPassword())
                                    .setParameter(7, student.getId())
                                    .executeUpdate();
                            if (updatedRows == 0) {
                                throw new NoResultException("Student with id " + student.getId() + " not found");
                            }
                        }
                    }
                }

                transaction.commit();
            } catch (PersistenceException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new PersistenceException("Error saving university", e);
            }
        }
    }

    @Override
    public void delete(University university) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            final EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                if (university != null && university.getId() != null) {
                    // Universitetin tələbələrinin olub-olmadığı yoxlanılır
                    Long studentCount = (Long) entityManager.createQuery(
                                    "SELECT COUNT(s) FROM students s WHERE s.university_id = :universityId")
                            .setParameter("universityId", university.getId())
                            .getSingleResult();

                    if (studentCount > 0) {
                        throw new IllegalStateException("Cannot delete university with students assigned to it");
                    }

                    // Əgər tələbə yoxdursa universitet silinir
                    int deletedRows = entityManager.createNativeQuery("DELETE FROM universities WHERE id = ?")
                            .setParameter(1, university.getId())
                            .executeUpdate();
                    if (deletedRows == 0) {
                        throw new NoResultException("University with id " + university.getId() + " not found");
                    }
                }
                transaction.commit();
            } catch (PersistenceException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new PersistenceException("Error deleting university", e);
            }
        }
    }

    @Override
    public void deleteById(long id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            final EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                // Universitetin tələbələrinin olub-olmadığını yoxlayır
                Long studentCount = (Long) entityManager.createQuery(
                                "SELECT COUNT(s) FROM students s WHERE s.university_id = :id")
                        .setParameter("id", id)
                        .getSingleResult();

                if (studentCount > 0) {
                    throw new IllegalStateException("Cannot delete university with students assigned to it");
                }

                // Universiteti silir
                int deletedRows = entityManager.createNativeQuery("DELETE FROM universities WHERE id = :id")
                        .setParameter("id", id)
                        .executeUpdate();

                if (deletedRows == 0) {
                    throw new NoResultException("University with id " + id + " not found");
                }
                transaction.commit();
            } catch (PersistenceException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new PersistenceException("Error deleting university with id: " + id, e);
            }
        }
    }

    @Override
    public long count() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return ((Number) entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM universities").getSingleResult()).longValue();
        } catch (PersistenceException e) {
            throw new PersistenceException("Error counting universities", e);
        }
    }

    @Override
    public List<University> filter(String name, String studentName, String teacherName) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT DISTINCT u.id, u.name FROM universities u " +
                            "LEFT JOIN students s ON s.university_id = u.id " +
                            "LEFT JOIN teachers t ON t.university_id = u.id WHERE 1=1 ");

            Map<String, Object> parameters = new HashMap<>();

            if (name != null && !name.isEmpty()) {
                sql.append("AND LOWER(u.name) LIKE :name ");
                parameters.put("name", "%" + name.toLowerCase() + "%");
            }
            if (studentName != null && !studentName.isEmpty()) {
                sql.append("AND LOWER(s.name) LIKE :studentName ");
                parameters.put("studentName", "%" + studentName.toLowerCase() + "%");
            }
            if (teacherName != null && !teacherName.isEmpty()) {
                sql.append("AND LOWER(t.name) LIKE :teacherName ");
                parameters.put("teacherName", "%" + teacherName.toLowerCase() + "%");
            }

            Query query = entityManager.createNativeQuery(sql.toString());

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            List<Object[]> results = query.getResultList();
            List<University> universities = new ArrayList<>();

            for (Object[] row : results) {
                University university = new University();
                university.setId(((Number) row[0]).longValue());
                university.setName((String) row[1]);

                List<Object[]> studentResults = (List<Object[]>) entityManager.createNativeQuery("SELECT " +
                        "s.id, s.name, s.surname, s.email, s.age FROM students s WHERE s.university_id =" +
                        " :universityId").setParameter("universityId", university.getId()).getResultList();
                List<Student> students = new ArrayList<>();

                // Tələbələr varsa, onları əlavə edirik
                if (!studentResults.isEmpty()) {
                    for (Object[] studentRow : studentResults) {
                        Student student = new Student();
                        student.setId(((Number) studentRow[0]).longValue());
                        student.setName((String) studentRow[1]);
                        student.setSurname((String) studentRow[2]);
                        student.setEmail((String) studentRow[3]);
                        student.setAge((studentRow[4] != null) ? ((Number) studentRow[4]).intValue() : null);
                        students.add(student);
                    }
                }
                university.setStudents(students);

                universities.add(university);
            }

            return universities;
        }
    }
}
