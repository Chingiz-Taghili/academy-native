package com.academy.repository.impls;

import com.academy.entity.Teacher;
import com.academy.entity.University;
import com.academy.repository.TeacherRepository;
import jakarta.persistence.*;

import java.util.*;

public class TeacherRepositoryImpl implements TeacherRepository {
    private final EntityManagerFactory entityManagerFactory;

    public TeacherRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<Teacher> findAll() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<Object[]> results = entityManager.createNativeQuery("SELECT " +
                    "t.id, t.name, t.surname, t.age, t.email, t.password, u.id, u.name FROM teachers t " +
                    "LEFT JOIN universities u ON t.university_id = u.id").getResultList();

            List<Teacher> teachers = new ArrayList<>();
            for (Object[] row : results) {
                Teacher teacher = new Teacher();
                teacher.setId(((Number) row[0]).longValue());
                teacher.setName((String) row[1]);
                teacher.setSurname((String) row[2]);
                teacher.setAge(row[3] != null ? ((Number) row[3]).intValue() : null);
                teacher.setEmail((String) row[4]);
                teacher.setPassword((String) row[5]);
                // Universiteti yaratmaq
                University university = new University();
                if (row[6] != null) {  // Universitet ID null ola bilər
                    university.setId(((Number) row[6]).longValue());
                    university.setName((String) row[7]);
                }
                teacher.setUniversity(university);
                teachers.add(teacher);
            }
            return teachers;
        }
    }

    @Override
    public Optional<Teacher> findById(long id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {
                Object[] result = (Object[]) entityManager.createNativeQuery("SELECT " +
                        "t.id, t.name, t.surname, t.email, t.age, t.university_id, u.id, u.name FROM teachers t " +
                        "LEFT JOIN universities u ON t.university_id = u.id WHERE t.id = :id"
                ).setParameter("id", id).getSingleResult();

                // Teacher obyektini yaradırıq
                Teacher teacher = new Teacher();
                teacher.setId(((Number) result[0]).longValue());
                teacher.setName((String) result[1]);
                teacher.setSurname((String) result[2]);
                teacher.setEmail((String) result[3]);
                teacher.setAge(result[4] != null ? ((Number) result[4]).intValue() : null);
                // University obyektini bağlayırıq
                if (result[5] != null) {
                    University university = new University();
                    university.setId(((Number) result[6]).longValue());
                    university.setName((String) result[7]);
                    teacher.setUniversity(university);
                }
                return Optional.of(teacher);
            } catch (NoResultException e) {
                return Optional.empty(); //Hibernate-in "NoResultException"-ı olduqda Optional.empty() qaytarır
            }
        }
    }

    @Override
    public List<Teacher> findByName(String name) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<Object[]> results = entityManager.createNativeQuery("SELECT " +
                    "t.id, t.name, t.surname, t.email, t.age, t.password, u.id, u.name FROM teachers t " +
                    "LEFT JOIN universities u ON t.university_id = u.id WHERE t.name = :name"
            ).setParameter("name", name).getResultList();

            List<Teacher> teachers = new ArrayList<>();
            for (Object[] row : results) {
                Teacher teacher = new Teacher();
                teacher.setId(((Number) row[0]).longValue());
                teacher.setName((String) row[1]);
                teacher.setSurname((String) row[2]);
                teacher.setEmail((String) row[3]);
                teacher.setAge(row[4] != null ? ((Number) row[4]).intValue() : null);
                teacher.setPassword((String) row[5]);
                // Universiteti yaratmaq
                University university = null;
                if (row[6] != null) {  // Universitet ID null ola bilər
                    university = new University();
                    university.setId(((Number) row[6]).longValue());
                    university.setName((String) row[7]);
                }
                teacher.setUniversity(university);
                teachers.add(teacher);
            }
            return teachers;
        }
    }

    @Override
    public void save(Teacher teacher) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                if (teacher.getId() == null) {
                    //id olmadiqda servere yeni Teacher elave edir
                    entityManager.createNativeQuery("INSERT INTO teachers (name, surname, " +
                                    "age, email, university_id, password) VALUES (?, ?, ?, ?, ?, ?)")
                            .setParameter(1, teacher.getName())
                            .setParameter(2, teacher.getSurname())
                            .setParameter(3, teacher.getAge())
                            .setParameter(4, teacher.getEmail())
                            .setParameter(5, teacher.getUniversity().getId())
                            .setParameter(6, teacher.getPassword())
                            .executeUpdate();
                } else {
                    //serverde id-ye uygun Teacher tapildiqda onu yenileyir
                    int updatedRows = entityManager.createNativeQuery("UPDATE teachers SET name = ?, " +
                                    "surname = ?, age = ?, email = ?, university_id = ?, password = ? WHERE id = ?")
                            .setParameter(1, teacher.getName())
                            .setParameter(2, teacher.getSurname())
                            .setParameter(3, teacher.getAge())
                            .setParameter(4, teacher.getEmail())
                            .setParameter(5, teacher.getUniversity().getId())
                            .setParameter(6, teacher.getPassword())
                            .setParameter(7, teacher.getId())
                            .executeUpdate();
                    if (updatedRows == 0) {
                        throw new NoResultException("Teacher with id " + teacher.getId() + " not found");
                    }
                }
                transaction.commit();
            } catch (PersistenceException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new PersistenceException("Error saving teacher", e);
            }
        }
    }

    @Override
    public void delete(Teacher teacher) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            final EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                if (teacher != null && teacher.getId() != null) {
                    int deletedRows = entityManager.createNativeQuery("DELETE FROM teachers WHERE id = ?")
                            .setParameter(1, teacher.getId()).executeUpdate();
                    if (deletedRows == 0) {
                        throw new NoResultException("Teacher with id " + teacher.getId() + " not found");
                    }
                }
                transaction.commit();
            } catch (PersistenceException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new PersistenceException("Error deleting teacher", e);
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
                        "DELETE FROM teachers WHERE id = :id").setParameter("id", id).executeUpdate();
                if (deletedRows == 0) {
                    throw new NoResultException("Teacher with id " + id + " not found");
                }
                transaction.commit();
            } catch (PersistenceException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new PersistenceException("Error deleting teacher with id: " + id, e);
            }
        }
    }

    @Override
    public long count() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return ((Number) entityManager.createNativeQuery(
                    "SELECT COUNT(*) FROM teachers").getSingleResult()).longValue();
        } catch (PersistenceException e) {
            throw new PersistenceException("Error counting teachers", e);
        }
    }

    @Override
    public List<Teacher> filter(String name, String surname, String email, String universityName, Integer age) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT t.id, t.name, t.surname, t.email, t.age, " +
                            "u.id as university_id, u.name as university_name FROM teachers t ");
            sql.append("LEFT JOIN universities u ON t.university_id = u.id WHERE 1=1 ");
            Map<String, Object> parameters = new HashMap<>();
            if (name != null && !name.isEmpty()) {
                sql.append("AND LOWER(t.name) LIKE :name ");
                parameters.put("name", "%" + name.toLowerCase() + "%");
            }
            if (surname != null && !surname.isEmpty()) {
                sql.append("AND LOWER(t.surname) LIKE :surname ");
                parameters.put("surname", "%" + surname.toLowerCase() + "%");
            }
            if (email != null && !email.isEmpty()) {
                sql.append("AND LOWER(t.email) LIKE :email ");
                parameters.put("email", "%" + email.toLowerCase() + "%");
            }
            if (universityName != null && !universityName.isEmpty()) {
                sql.append("AND LOWER(u.name) LIKE :universityName ");
                parameters.put("universityName", "%" + universityName.toLowerCase() + "%");
            }
            if (age != null) {
                sql.append("AND t.age = :age ");
                parameters.put("age", age);
            }
            Query query = entityManager.createNativeQuery(sql.toString());
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            List<Object[]> results = query.getResultList();
            List<Teacher> teachers = new ArrayList<>();
            for (Object[] row : results) {
                Teacher teacher = new Teacher();
                teacher.setId(((Number) row[0]).longValue());
                teacher.setName((String) row[1]);
                teacher.setSurname((String) row[2]);
                teacher.setEmail((String) row[3]);
                teacher.setAge((row[4] != null) ? ((Number) row[4]).intValue() : null);

                University university = null;
                if (row[5] != null) {
                    university = new University();
                    university.setId(((Number) row[5]).longValue());
                    university.setName((String) row[6]);
                }
                teacher.setUniversity(university);
                teachers.add(teacher);
            }
            return teachers;
        }
    }
}
