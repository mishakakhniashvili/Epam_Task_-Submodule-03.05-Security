package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {

    @Query("select tt from TrainingType tt where tt.trainingTypeName = :name")
    Optional<TrainingType> findByName(@Param("name") String name);
}