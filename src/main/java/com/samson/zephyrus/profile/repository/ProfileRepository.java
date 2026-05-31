package com.samson.zephyrus.profile.repository;

import com.samson.zephyrus.profile.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    List<Profile> findByUserIdOrderBySortOrder(UUID userId);

    int countByUserId(UUID userId);

    boolean existsByUserIdAndName(UUID userId, String name);
}