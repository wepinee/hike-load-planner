package ru.hikeload.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.hikeload.domain.UserAccount;
import ru.hikeload.repository.UserAccountRepository;

@Configuration
public class DevDataInitializer {

    @Bean
    CommandLineRunner seedUser(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "organizer@hike.local";
            UserAccount user = userAccountRepository.findByEmail(email).orElseGet(() -> {
                UserAccount created = new UserAccount();
                created.setEmail(email);
                created.setDisplayName("Организатор");
                return created;
            });
            user.setPasswordHash(passwordEncoder.encode("demo"));
            userAccountRepository.save(user);
        };
    }
}
