package com.samson.zephyrus.content.service;

import com.samson.zephyrus.content.dto.*;
import com.samson.zephyrus.content.model.*;
import com.samson.zephyrus.content.model.MediaType;
import com.samson.zephyrus.content.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core content service that assembles browse rows, featured titles,
 * title details, episode listings, and search results from the local DB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {

    private static final int FEATURED_COUNT = 8;
    private static final int ROW_PAGE_SIZE = 20;
    private static final int OVERVIEW_TRUNCATE_LENGTH = 200;

    private final TitleRepository titleRepository;
    private final GenreRepository genreRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final CastMemberRepository castMemberRepository;
    private final ContentRowRepository contentRowRepository;

    // ═══════════════════════════════════════════════════════
    // Browse rows
    // ═══════════════════════════════════════════════════════

    /**
     * Returns pre-assembled content rows for the home page.
     */
    public List<ContentRowDto> getBrowseRows() {
        List<ContentRow> rows = contentRowRepository.findAllByOrderBySortOrder();
        return rows.stream().map(this::toContentRowDto).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════
    // Featured / Hero billboard
    // ═══════════════════════════════════════════════════════

    public FeaturedDto getFeatured() {
        Pageable pageable = PageRequest.of(0, FEATURED_COUNT, Sort.by(Sort.Direction.DESC, "popularity"));
        Page<Title> page = titleRepository.findTopByPopularity(pageable);
        List<TitleSummaryDto> titles = page.getContent().stream()
                .map(this::toTitleSummaryDto)
                .collect(Collectors.toList());
        return FeaturedDto.builder().titles(titles).build();
    }

    // ═══════════════════════════════════════════════════════
    // Title detail
    // ═══════════════════════════════════════════════════════

    public TitleDetailDto getTitleDetail(UUID id) {
        Title title = titleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Title not found: " + id));

        List<CastMemberDto> cast = castMemberRepository.findByTitleIdOrderByDisplayOrder(id)
                .stream().map(this::toCastMemberDto).collect(Collectors.toList());

        List<SeasonDto> seasons = Collections.emptyList();
        if (title.getMediaType() == MediaType.SERIES) {
            seasons = seasonRepository.findByTitleIdOrderBySeasonNumber(id)
                    .stream().map(this::toSeasonDto).collect(Collectors.toList());
        }

        // Similar titles: same genres, exclude self
        List<TitleSummaryDto> similar = getSimilar(id);

        return TitleDetailDto.builder()
                .id(title.getId())
                .tmdbId(title.getTmdbId())
                .mediaType(title.getMediaType().name())
                .title(title.getTitle())
                .overview(title.getOverview())
                .tagline(title.getTagline())
                .releaseDate(title.getReleaseDate())
                .runtime(title.getRuntime())
                .posterPath(title.getPosterPath())
                .backdropPath(title.getBackdropPath())
                .voteAverage(title.getVoteAverage())
                .popularity(title.getPopularity())
                .originalLanguage(title.getOriginalLanguage())
                .maturityRating(title.getMaturityRating())
                .status(title.getStatus())
                .createdAt(title.getCreatedAt())
                .updatedAt(title.getUpdatedAt())
                .genres(title.getGenres().stream().map(this::toGenreDto).collect(Collectors.toList()))
                .cast(cast)
                .seasons(seasons)
                .similarTitles(similar)
                .build();
    }

    // ═══════════════════════════════════════════════════════
    // Episodes
    // ═══════════════════════════════════════════════════════

    public List<EpisodeDto> getEpisodes(UUID titleId, int seasonNumber) {
        List<Season> seasons = seasonRepository.findByTitleIdOrderBySeasonNumber(titleId);
        Season season = seasons.stream()
                .filter(s -> s.getSeasonNumber() == seasonNumber)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Season " + seasonNumber + " not found for title " + titleId));

        return episodeRepository.findBySeasonIdOrderByEpisodeNumber(season.getId())
                .stream().map(this::toEpisodeDto).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════
    // Similar titles
    // ═══════════════════════════════════════════════════════

    public List<TitleSummaryDto> getSimilar(UUID titleId) {
        Title title = titleRepository.findById(titleId)
                .orElseThrow(() -> new EntityNotFoundException("Title not found: " + titleId));

        if (title.getGenres().isEmpty()) {
            return Collections.emptyList();
        }

        // Pick the first genre and grab titles from it
        UUID genreId = title.getGenres().getFirst().getId();
        Page<Title> page = titleRepository.findByGenreId(genreId, PageRequest.of(0, 12));
        return page.getContent().stream()
                .filter(t -> !t.getId().equals(titleId))
                .map(this::toTitleSummaryDto)
                .limit(10)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════
    // Search
    // ═══════════════════════════════════════════════════════

    public SearchResultDto searchTitles(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Title> results = titleRepository.searchByTitle(query, pageable);
        List<TitleSummaryDto> titles = results.getContent().stream()
                .map(this::toTitleSummaryDto)
                .collect(Collectors.toList());

        return SearchResultDto.builder()
                .results(titles)
                .page(results.getNumber())
                .size(results.getSize())
                .totalElements(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .build();
    }

    // ═══════════════════════════════════════════════════════
    // Genres
    // ═══════════════════════════════════════════════════════

    public List<GenreDto> getGenres() {
        return genreRepository.findAll().stream()
                .map(this::toGenreDto)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════
    // Browse by genre
    // ═══════════════════════════════════════════════════════

    public SearchResultDto browseByGenre(UUID genreId, String sort, int page, int size) {
        Sort jpaSort = switch (sort != null ? sort.toLowerCase() : "popularity") {
            case "vote_average", "rating" -> Sort.by(Sort.Direction.DESC, "voteAverage");
            case "release_date", "newest" -> Sort.by(Sort.Direction.DESC, "releaseDate");
            default -> Sort.by(Sort.Direction.DESC, "popularity");
        };

        Pageable pageable = PageRequest.of(page, size, jpaSort);
        Page<Title> results = titleRepository.findByGenreId(genreId, pageable);
        List<TitleSummaryDto> titles = results.getContent().stream()
                .map(this::toTitleSummaryDto)
                .collect(Collectors.toList());

        return SearchResultDto.builder()
                .results(titles)
                .page(results.getNumber())
                .size(results.getSize())
                .totalElements(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .build();
    }

    // ═══════════════════════════════════════════════════════
    // ── Mapping helpers ───────────────────────────────────
    // ═══════════════════════════════════════════════════════

    private TitleSummaryDto toTitleSummaryDto(Title title) {
        String overview = title.getOverview();
        if (overview != null && overview.length() > OVERVIEW_TRUNCATE_LENGTH) {
            overview = overview.substring(0, OVERVIEW_TRUNCATE_LENGTH) + "…";
        }

        List<String> genreNames = title.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toList());

        return TitleSummaryDto.builder()
                .id(title.getId())
                .tmdbId(title.getTmdbId())
                .mediaType(title.getMediaType().name())
                .title(title.getTitle())
                .overview(overview)
                .posterPath(title.getPosterPath())
                .backdropPath(title.getBackdropPath())
                .voteAverage(title.getVoteAverage())
                .releaseDate(title.getReleaseDate())
                .maturityRating(title.getMaturityRating())
                .genres(genreNames)
                .build();
    }

    private GenreDto toGenreDto(Genre genre) {
        return GenreDto.builder()
                .id(genre.getId())
                .tmdbId(genre.getTmdbId())
                .name(genre.getName())
                .build();
    }

    private SeasonDto toSeasonDto(Season season) {
        return SeasonDto.builder()
                .id(season.getId())
                .seasonNumber(season.getSeasonNumber())
                .name(season.getName())
                .overview(season.getOverview())
                .posterPath(season.getPosterPath())
                .episodeCount(season.getEpisodeCount())
                .airDate(season.getAirDate())
                .build();
    }

    private EpisodeDto toEpisodeDto(Episode episode) {
        return EpisodeDto.builder()
                .id(episode.getId())
                .episodeNumber(episode.getEpisodeNumber())
                .name(episode.getName())
                .overview(episode.getOverview())
                .stillPath(episode.getStillPath())
                .runtime(episode.getRuntime())
                .airDate(episode.getAirDate())
                .build();
    }

    private CastMemberDto toCastMemberDto(CastMember member) {
        return CastMemberDto.builder()
                .id(member.getId())
                .tmdbPersonId(member.getTmdbPersonId())
                .name(member.getName())
                .characterName(member.getCharacterName())
                .profilePath(member.getProfilePath())
                .displayOrder(member.getDisplayOrder())
                .build();
    }

    private ContentRowDto toContentRowDto(ContentRow row) {
        List<TitleSummaryDto> titles;
        Pageable pageable = PageRequest.of(0, ROW_PAGE_SIZE);

        titles = switch (row.getRowType()) {
            case TRENDING -> titleRepository.findTopByPopularity(pageable)
                    .getContent().stream().map(this::toTitleSummaryDto).collect(Collectors.toList());
            case NEW_RELEASES -> titleRepository.findLatest(pageable)
                    .getContent().stream().map(this::toTitleSummaryDto).collect(Collectors.toList());
            case TOP_RATED -> titleRepository.findTopByVoteAverage(pageable)
                    .getContent().stream().map(this::toTitleSummaryDto).collect(Collectors.toList());
            case GENRE -> {
                if (row.getGenre() != null) {
                    yield titleRepository.findByGenreId(row.getGenre().getId(), pageable)
                            .getContent().stream().map(this::toTitleSummaryDto).collect(Collectors.toList());
                }
                yield Collections.emptyList();
            }
        };

        return ContentRowDto.builder()
                .label(row.getLabel())
                .rowType(row.getRowType().name())
                .titles(titles)
                .build();
    }
}
