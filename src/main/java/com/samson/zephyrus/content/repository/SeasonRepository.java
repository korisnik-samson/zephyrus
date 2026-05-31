package com.samson.zephyrus.content.repository;

import com.samson.zephyrus.content.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeasonRepository extends JpaRepository<Season, UUID> {

    List<Season> findByTitleIdOrderBySeasonNumber(UUID titleId);

    Optional<Season> findByTitleIdAndSeasonNumber(UUID titleId, int seasonNumber);
}
