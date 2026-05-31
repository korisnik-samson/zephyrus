package com.samson.zephyrus.profile.repository;

import com.samson.zephyrus.profile.model.MyListEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MyListRepository extends JpaRepository<MyListEntry, UUID> {

    List<MyListEntry> findByProfileIdOrderByAddedAtDesc(UUID profileId);

    Optional<MyListEntry> findByProfileIdAndTitleId(UUID profileId, UUID titleId);

    boolean existsByProfileIdAndTitleId(UUID profileId, UUID titleId);

    void deleteByProfileIdAndTitleId(UUID profileId, UUID titleId);
}