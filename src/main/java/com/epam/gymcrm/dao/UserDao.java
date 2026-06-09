package com.epam.gymcrm.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {
    @PersistenceContext
    private EntityManager entityManager;

    public List<String> findAllUsernames(){
        return entityManager
                .createQuery("select u.username from User u", String.class)
                .getResultList();
    }

}