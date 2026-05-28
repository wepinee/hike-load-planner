package ru.hikeload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.hikeload.domain.UserAccount;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
