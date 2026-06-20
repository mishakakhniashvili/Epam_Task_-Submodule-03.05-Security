package com.epam.gymcrm.health;

import com.epam.gymcrm.repository.UserRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
@Component
public class UserRepositoryHealthIndicator  implements HealthIndicator {
        private final UserRepository userRepository;

        public UserRepositoryHealthIndicator(UserRepository userRepository){
            this.userRepository = userRepository;
        }
        @Override
        public Health health() {
            try {
                long count = userRepository.count();
                return Health.up()
                        .withDetail("usersCount", count)
                        .build();
            }
            catch (Exception ex){
                return Health.down(ex).build();
            }

        }
}
