package org.file.cloud.repository;

import org.file.cloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    @Query("select u.id from User u where lower(u.username) = lower(:username)")
    Long findIdByUsernameIgnoreCase(String username);
}
