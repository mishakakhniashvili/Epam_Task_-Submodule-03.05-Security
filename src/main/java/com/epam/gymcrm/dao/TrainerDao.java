package com.epam.gymcrm.dao;


import com.epam.gymcrm.entity.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDao {

    @PersistenceContext
    private EntityManager entityManager;


    public Trainer save(Trainer trainer) {
        entityManager.persist(trainer);
        return trainer;
    }

    public Trainer update(Trainer trainer) {
        return entityManager.merge(trainer);
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainer.class, id));
    }

    public List<Trainer> findAll() {
        return entityManager
                .createQuery("select t from Trainer t", Trainer.class)
                .getResultList();
    }

    public Optional<Trainer> findByUsername(String username) {
        return entityManager
                .createQuery("select t from Trainer t where t.user.username = :username", Trainer.class)
                .setParameter("username", username)
                .getResultStream().findFirst();
    }

    public List<Trainer> findTrainersNotAssignedToTrainee(String traineeUsername){
        return entityManager
                .createQuery("select tr from Trainer tr " +
                                "where tr not in (" +
                                "select assignedTrainer from Trainee te " +
                                "join te.trainers assignedTrainer " +
                                "where te.user.username = :traineeUsername" +
                                ")"
                        , Trainer.class)
                .setParameter("traineeUsername", traineeUsername)
                .getResultList();
    }
}