package com.epam.gymcrm.dao;

import com.epam.gymcrm.entity.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDao {

    @PersistenceContext
    private EntityManager entityManager;


    public TrainingType save(TrainingType trainingType) {
        entityManager.persist(trainingType);
        return trainingType;
    }

    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainingType.class, id));
    }

    public Optional<TrainingType> findByName(String trainingTypeName) {
        return entityManager
                .createQuery("select tt from TrainingType tt where tt.trainingTypeName = :trainingTypeName", TrainingType.class)
                .setParameter("trainingTypeName", trainingTypeName)
                .getResultStream()
                .findFirst();
    }

    public List<TrainingType> findAll() {
        return entityManager.createQuery("select tt from TrainingType tt", TrainingType.class).getResultList();
    }


}
