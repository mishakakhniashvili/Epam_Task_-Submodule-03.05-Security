package com.epam.gymcrm.dao;

import com.epam.gymcrm.entity.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Trainee save(Trainee trainee) {
        entityManager.persist(trainee);
        return trainee;
    }

    public Trainee update(Trainee trainee) {
        return entityManager.merge(trainee);
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainee.class, id));
    }

    public List<Trainee> findAll() {
        return entityManager
                .createQuery("select t from Trainee t", Trainee.class)
                .getResultList();
    }

    public Optional<Trainee> findByUsername(String username) {
        return entityManager
                .createQuery(
                        "select t from Trainee t where t.user.username = :username",
                        Trainee.class
                )
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

}