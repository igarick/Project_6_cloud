package org.file.cloud.repository;

import org.file.cloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
//    Optional<User> findUserByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);
}
