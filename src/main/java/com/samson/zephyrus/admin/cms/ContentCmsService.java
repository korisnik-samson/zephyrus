package com.samson.zephyrus.admin.cms;

import com.fasterxml.jackson.databind.JsonNode;
import com.samson.zephyrus.admin.cms.dto.*;
import com.samson.zephyrus.content.dto.CastMemberDto;
import com.samson.zephyrus.content.dto.GenreDto;
import com.samson.zephyrus.content.dto.SeasonDto;
import com.samson.zephyrus.content.model.*;
import com.samson.zephyrus.content.repository.*;
import com.samson.zephyrus.content.service.TmdbClient;
import com.samson.zephyrus.search.MeilisearchSyncService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ContentCmsService {

    private final TitleRepository titleRepository;
    private final GenreRepository genreRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final CastMemberRepository castMemberRepository;
    private final TmdbClient tmdbClient;
    private final MeilisearchSyncService meilisearchSyncService;

    // ── Catalog listing ───────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminTitleDto> listTitles(int page, int size) {
        return titleRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toAdminDto);
    }

    // ── Manual CRUD ───────────────────────────────────────

    @Transactional
    public AdminTitleDto createTitle(CreateTitleRequest req) {
        Title title = Title.builder()
                .mediaType(MediaType.valueOf(req.getMediaType()))
                .title(req.getTitle())
                .overview(req.getOverview())
                .tagline(req.getTagline())
                .releaseDate(req.getReleaseDate())
                .runtime(req.getRuntime())
                .posterPath(req.getPosterPath())
                .backdropPath(req.getBackdropPath())
                .voteAverage(req.getVoteAverage())
                .popularity(req.getPopularity())
                .originalLanguage(req.getOriginalLanguage())
                .maturityRating(req.getMaturityRating())
                .status(req.getStatus())
                .published(req.isPublished())
                .genres(resolveGenres(req.getGenreIds()))
                .build();

        title = titleRepository.save(title);
        meilisearchSyncService.indexTitle(title);
        log.info("Admin created title '{}'", title.getTitle());
        return toAdminDto(title);
    }

    @Transactional
    public AdminTitleDto updateTitle(UUID id, UpdateTitleRequest req) {
        Title title = requireTitle(id);

        if (req.getTitle() != null) title.setTitle(req.getTitle());
        if (req.getOverview() != null) title.setOverview(req.getOverview());
        if (req.getTagline() != null) title.setTagline(req.getTagline());
        if (req.getReleaseDate() != null) title.setReleaseDate(req.getReleaseDate());
        if (req.getRuntime() != null) title.setRuntime(req.getRuntime());
        if (req.getPosterPath() != null) title.setPosterPath(req.getPosterPath());
        if (req.getBackdropPath() != null) title.setBackdropPath(req.getBackdropPath());
        if (req.getVoteAverage() != null) title.setVoteAverage(req.getVoteAverage());
        if (req.getPopularity() != null) title.setPopularity(req.getPopularity());
        if (req.getOriginalLanguage() != null) title.setOriginalLanguage(req.getOriginalLanguage());
        if (req.getMaturityRating() != null) title.setMaturityRating(req.getMaturityRating());
        if (req.getStatus() != null) title.setStatus(req.getStatus());
        if (req.getPublished() != null) title.setPublished(req.getPublished());
        if (req.getGenreIds() != null) title.setGenres(resolveGenres(req.getGenreIds()));

        title = titleRepository.save(title);
        meilisearchSyncService.indexTitle(title);
        return toAdminDto(title);
    }

    @Transactional
    public void deleteTitle(UUID id) {
        Title title = requireTitle(id);
        titleRepository.delete(title);
        meilisearchSyncService.deleteTitle(id);
        log.info("Admin deleted title id={}", id);
    }

    // ── Publish / schedule ────────────────────────────────

    @Transactional
    public AdminTitleDto setPublished(UUID id, boolean published) {
        Title title = requireTitle(id);
        title.setPublished(published);
        return toAdminDto(titleRepository.save(title));
    }

    @Transactional
    public AdminTitleDto scheduleContent(UUID id, ScheduleContentRequest req) {
        Title title = requireTitle(id);
        title.setAvailableFrom(req.getAvailableFrom());
        title.setAvailableUntil(req.getAvailableUntil());
        return toAdminDto(titleRepository.save(title));
    }

    // ── TMDB bulk import ──────────────────────────────────

    @Transactional
    public List<AdminTitleDto> bulkImport(BulkImportRequest req) {
        List<AdminTitleDto> results = new ArrayList<>();
        for (BulkImportRequest.ImportItem item : req.getItems()) {
            try {
                results.add(importFromTmdb(item.getTmdbId(), item.getMediaType()));
            } catch (Exception e) {
                log.error("Import failed for tmdbId={} mediaType={}: {}", item.getTmdbId(), item.getMediaType(), e.getMessage());
            }
        }
        return results;
    }

    @Transactional
    public AdminTitleDto importFromTmdb(int tmdbId, String tmdbMediaType) {
        boolean isMovie = "movie".equals(tmdbMediaType);
        JsonNode node = isMovie
                ? tmdbClient.getMovieDetails(tmdbId)
                : tmdbClient.getTvDetails(tmdbId);

        MediaType mediaType = isMovie ? MediaType.MOVIE : MediaType.SERIES;
        Title title = titleRepository.findByTmdbId(tmdbId).orElseGet(Title::new);

        // ── Core fields ──
        title.setTmdbId(tmdbId);
        title.setMediaType(mediaType);
        title.setTitle(isMovie ? node.path("title").asText() : node.path("name").asText());
        title.setOverview(textOrNull(node, "overview"));
        title.setTagline(textOrNull(node, "tagline"));
        title.setPosterPath(textOrNull(node, "poster_path"));
        title.setBackdropPath(textOrNull(node, "backdrop_path"));
        title.setVoteAverage(decimalOrNull(node, "vote_average"));
        title.setPopularity(decimalOrNull(node, "popularity"));
        title.setOriginalLanguage(textOrNull(node, "original_language"));
        title.setStatus(textOrNull(node, "status"));

        String dateStr = isMovie ? node.path("release_date").asText("") : node.path("first_air_date").asText("");
        title.setReleaseDate(parseDateOrNull(dateStr));

        if (isMovie) {
            title.setRuntime(node.path("runtime").asInt(0) > 0 ? node.path("runtime").asInt() : null);
        } else {
            JsonNode runtimes = node.path("episode_run_time");
            if (runtimes.isArray() && runtimes.size() > 0) {
                title.setRuntime(runtimes.get(0).asInt());
            }
        }

        // ── Genres ──
        title.setGenres(mapGenres(node.path("genres")));
        title = titleRepository.save(title);

        // ── Cast (replace) ──
        importCast(title, node.path("credits").path("cast"));

        // ── Seasons + Episodes (TV only) ──
        if (!isMovie) {
            int numSeasons = node.path("number_of_seasons").asInt(0);
            for (int s = 1; s <= numSeasons; s++) {
                importSeason(title, tmdbId, s);
            }
        }

        meilisearchSyncService.indexTitle(title);
        log.info("Imported from TMDB: {} ({})", title.getTitle(), tmdbMediaType);
        return toAdminDto(title);
    }

    // ── Private helpers ───────────────────────────────────

    private void importCast(Title title, JsonNode castNode) {
        if (!castNode.isArray()) return;
        castMemberRepository.deleteByTitleId(title.getId());
        List<CastMember> cast = new ArrayList<>();
        int order = 0;
        for (JsonNode c : castNode) {
            if (order >= 20) break;
            cast.add(CastMember.builder()
                    .title(title)
                    .tmdbPersonId(c.path("id").asInt())
                    .name(c.path("name").asText())
                    .characterName(c.path("character").asText(null))
                    .profilePath(c.path("profile_path").asText(null))
                    .displayOrder(order++)
                    .build());
        }
        castMemberRepository.saveAll(cast);
    }

    private void importSeason(Title title, int tmdbId, int seasonNumber) {
        try {
            JsonNode seasonNode = tmdbClient.getSeasonDetails(tmdbId, seasonNumber);

            Season season = seasonRepository
                    .findByTitleIdAndSeasonNumber(title.getId(), seasonNumber)
                    .orElseGet(Season::new);

            season.setTitle(title);
            season.setSeasonNumber(seasonNumber);
            season.setName(seasonNode.path("name").asText(null));
            season.setOverview(seasonNode.path("overview").asText(null));
            season.setPosterPath(seasonNode.path("poster_path").asText(null));
            season.setAirDate(parseDateOrNull(seasonNode.path("air_date").asText("")));

            JsonNode episodes = seasonNode.path("episodes");
            season.setEpisodeCount(episodes.isArray() ? episodes.size() : 0);
            season = seasonRepository.save(season);

            if (episodes.isArray()) {
                for (JsonNode ep : episodes) {
                    importEpisode(season, ep);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to import season {} for tmdbId={}: {}", seasonNumber, tmdbId, e.getMessage());
        }
    }

    private void importEpisode(Season season, JsonNode ep) {
        int epNum = ep.path("episode_number").asInt();
        Episode episode = episodeRepository
                .findBySeasonIdAndEpisodeNumber(season.getId(), epNum)
                .orElseGet(Episode::new);

        episode.setSeason(season);
        episode.setEpisodeNumber(epNum);
        episode.setName(ep.path("name").asText(null));
        episode.setOverview(ep.path("overview").asText(null));
        episode.setStillPath(ep.path("still_path").asText(null));
        episode.setRuntime(ep.path("runtime").asInt(0) > 0 ? ep.path("runtime").asInt() : null);
        episode.setAirDate(parseDateOrNull(ep.path("air_date").asText("")));

        episodeRepository.save(episode);
    }

    private List<Genre> mapGenres(JsonNode genresNode) {
        if (!genresNode.isArray()) return Collections.emptyList();
        List<Genre> genres = new ArrayList<>();
        for (JsonNode g : genresNode) {
            genreRepository.findByTmdbId(g.path("id").asInt()).ifPresent(genres::add);
        }
        return genres;
    }

    private List<Genre> resolveGenres(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return ids.stream()
                .map(id -> genreRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Title requireTitle(UUID id) {
        return titleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Title not found: " + id));
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode n = node.path(field);
        return n.isMissingNode() || n.isNull() || n.asText().isBlank() ? null : n.asText();
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode n = node.path(field);
        if (n.isMissingNode() || n.isNull()) return null;
        try { return new BigDecimal(n.asText()); } catch (Exception e) { return null; }
    }

    private LocalDate parseDateOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private AdminTitleDto toAdminDto(Title t) {
        List<CastMemberDto> cast = castMemberRepository.findByTitleIdOrderByDisplayOrder(t.getId())
                .stream().map(c -> CastMemberDto.builder()
                        .id(c.getId()).tmdbPersonId(c.getTmdbPersonId()).name(c.getName())
                        .characterName(c.getCharacterName()).profilePath(c.getProfilePath())
                        .displayOrder(c.getDisplayOrder()).build())
                .collect(Collectors.toList());

        List<SeasonDto> seasons = t.getMediaType() == MediaType.SERIES
                ? seasonRepository.findByTitleIdOrderBySeasonNumber(t.getId()).stream()
                        .map(s -> SeasonDto.builder().id(s.getId()).seasonNumber(s.getSeasonNumber())
                                .name(s.getName()).overview(s.getOverview()).posterPath(s.getPosterPath())
                                .episodeCount(s.getEpisodeCount()).airDate(s.getAirDate()).build())
                        .collect(Collectors.toList())
                : Collections.emptyList();

        List<GenreDto> genres = t.getGenres().stream()
                .map(g -> GenreDto.builder().id(g.getId()).tmdbId(g.getTmdbId()).name(g.getName()).build())
                .collect(Collectors.toList());

        return AdminTitleDto.builder()
                .id(t.getId()).tmdbId(t.getTmdbId()).mediaType(t.getMediaType().name())
                .title(t.getTitle()).overview(t.getOverview()).tagline(t.getTagline())
                .releaseDate(t.getReleaseDate()).runtime(t.getRuntime())
                .posterPath(t.getPosterPath()).backdropPath(t.getBackdropPath())
                .voteAverage(t.getVoteAverage()).popularity(t.getPopularity())
                .originalLanguage(t.getOriginalLanguage()).maturityRating(t.getMaturityRating())
                .status(t.getStatus()).published(t.isPublished())
                .availableFrom(t.getAvailableFrom() != null ? t.getAvailableFrom().toString() : null)
                .availableUntil(t.getAvailableUntil() != null ? t.getAvailableUntil().toString() : null)
                .createdAt(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null)
                .updatedAt(t.getUpdatedAt() != null ? t.getUpdatedAt().toString() : null)
                .genres(genres).cast(cast).seasons(seasons)
                .build();
    }
}