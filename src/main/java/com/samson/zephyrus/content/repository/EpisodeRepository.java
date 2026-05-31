package com.samson.zephyrus.content.repository;

import com.samson.zephyrus.content.model.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, UUID> {

    List<Episode> findBySeasonIdOrderByEpisodeNumber(UUID seasonId);

    Optional<Episode> findBySeasonIdAndEpisodeNumber(UUID seasonId, int episodeNumber);
}
