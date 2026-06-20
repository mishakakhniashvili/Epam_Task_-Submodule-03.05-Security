package com.epam.gymcrm.health;

import com.epam.gymcrm.repository.TrainingTypeRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeRepositoryHealthIndicator implements HealthIndicator{
    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeRepositoryHealthIndicator(TrainingTypeRepository trainingTypeRepository){
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        try {

            long count = trainingTypeRepository.count();
            if (count > 0) {
                return Health.up()
                        .withDetail("trainingTypesCount", count)
                        .build();
            }
            return Health.down()
                    .withDetail("reason", "No training types found")
                    .build();
            }
        catch (Exception ex){
                return Health.down(ex).build();
        }


    }
}
