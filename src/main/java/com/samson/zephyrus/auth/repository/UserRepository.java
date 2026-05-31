package com.samson.zephyrus.auth.repository;

import com.samson.zephyrus.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByCreatedAtAfter(LocalDateTime since);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(u.displayName) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<User> searchUsers(@Param("q") String query, Pageable pageable);

    @Query(
        value = "SELECT created_at::date::text, COUNT(*) FROM users WHERE created_at >= :since GROUP BY 1 ORDER BY 1 ASC",
        nativeQuery = true
    )
    List<Object[]> countRegistrationsByDay(@Param("since") LocalDateTime since);
}
