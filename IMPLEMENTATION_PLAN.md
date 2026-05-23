# Zephyrus — Backend Implementation Plan

> **Spring Boot 3.x • Java 21 • PostgreSQL 16 • Redis 7**
>
> Backend API service for the Zephyrus streaming platform.
> Frontend counterpart: [`stream`](../stream/)

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  stream (Next.js 15)                    │
│              Frontend @ localhost:3000                   │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP (REST)
┌──────────────────────▼──────────────────────────────────┐
│                 zephyrus (Spring Boot)                  │
│                 Backend @ localhost:8080                 │
│                                                         │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐ │
│  │   Auth   │ │ Content  │ │ Playback │ │  Profile  │ │
│  │ Service  │ │ Service  │ │ Service  │ │  Service  │ │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └─────┬─────┘ │
│       │             │            │              │       │
│  ┌────▼─────────────▼────────────▼──────────────▼────┐ │
│  │              Spring Data JPA (Hibernate)          │ │
│  └────┬──────────────────────────────────────┬───────┘ │
└───────┼──────────────────────────────────────┼─────────┘
   ┌────▼────┐                            ┌────▼────┐
   │PostgreSQL│                            │  Redis  │
   │  :5432   │                            │  :6379  │
   └──────────┘                            └─────────┘
```

---

## Tech Stack

| Layer | Technology | Notes |
|---|---|---|
| Framework | Spring Boot 3.x | Java 21, virtual threads ready |
| Security | Spring Security 6 + JWT (jjwt-api) | Stateless, Bearer token auth |
| OAuth2 | Spring Security OAuth2 Client | Google + GitHub providers |
| ORM | Spring Data JPA (Hibernate 6) | Repository pattern |
| Database | PostgreSQL 16 | Primary data store |
| Cache | Spring Data Redis (Lettuce) | Sessions, TMDB cache, rate limiting |
| Migrations | Flyway | Versioned SQL migrations |
| Validation | Jakarta Bean Validation | `@Valid`, `@NotBlank`, `@Email`, etc. |
| HTTP Client | RestClient (Spring 6.1+) | TMDB API integration |
| API Docs | SpringDoc OpenAPI 2.x | Swagger UI at `/swagger-ui.html` |
| Build | Maven (maven-wrapper) | `./mvnw` commands |
| Testing | JUnit 5, Mockito, MockMvc, Testcontainers | Unit + integration |
| Containerization | Docker (multi-stage) | JRE 21 slim runtime |

---

## Project Structure

```
zephyrus/
├── pom.xml
├── mvnw / mvnw.cmd
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── .gitignore
├── IMPLEMENTATION_PLAN.md
├── LICENSE
└── src/
    ├── main/
    │   ├── java/com/zephyrus/
    │   │   ├── ZephyrusApplication.java
    │   │   │
    │   │   ├── auth/
    │   │   │   ├── AuthController.java
    │   │   │   ├── AuthService.java
    │   │   │   ├── JwtService.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── OAuth2Config.java
    │   │   │   ├── dto/
    │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── AuthResponse.java
    │   │   │   │   └── RefreshRequest.java
    │   │   │   ├── model/
    │   │   │   │   ├── User.java
    │   │   │   │   ├── Role.java              (enum: USER, ADMIN)
    │   │   │   │   └── RefreshToken.java
    │   │   │   └── repository/
    │   │   │       ├── UserRepository.java
    │   │   │       └── RefreshTokenRepository.java
    │   │   │
    │   │   ├── content/
    │   │   │   ├── ContentController.java
    │   │   │   ├── ContentService.java
    │   │   │   ├── TmdbClient.java
    │   │   │   ├── TmdbCacheService.java
    │   │   │   ├── ContentSyncJob.java
    │   │   │   ├── GenreInitializer.java
    │   │   │   ├── dto/
    │   │   │   │   ├── TitleResponse.java
    │   │   │   │   ├── TitleDetailResponse.java
    │   │   │   │   ├── ContentRowResponse.java
    │   │   │   │   ├── EpisodeResponse.java
    │   │   │   │   └── SearchResultResponse.java
    │   │   │   ├── model/
    │   │   │   │   ├── Title.java
    │   │   │   │   ├── MediaType.java         (enum: MOVIE, SERIES)
    │   │   │   │   ├── Genre.java
    │   │   │   │   ├── Season.java
    │   │   │   │   ├── Episode.java
    │   │   │   │   ├── CastMember.java
    │   │   │   │   └── ContentRow.java
    │   │   │   └── repository/
    │   │   │       ├── TitleRepository.java
    │   │   │       ├── GenreRepository.java
    │   │   │       ├── SeasonRepository.java
    │   │   │       ├── EpisodeRepository.java
    │   │   │       └── CastMemberRepository.java
    │   │   │
    │   │   ├── playback/
    │   │   │   ├── PlaybackController.java
    │   │   │   ├── PlaybackService.java
    │   │   │   ├── WatchHistoryService.java
    │   │   │   ├── dto/
    │   │   │   │   ├── ProgressRequest.java
    │   │   │   │   ├── ProgressResponse.java
    │   │   │   │   └── ContinueWatchingResponse.java
    │   │   │   ├── model/
    │   │   │   │   └── WatchProgress.java
    │   │   │   └── repository/
    │   │   │       └── WatchProgressRepository.java
    │   │   │
    │   │   ├── profile/
    │   │   │   ├── ProfileController.java
    │   │   │   ├── ProfileService.java
    │   │   │   ├── dto/
    │   │   │   │   ├── CreateProfileRequest.java
    │   │   │   │   ├── UpdateProfileRequest.java
    │   │   │   │   └── ProfileResponse.java
    │   │   │   ├── model/
    │   │   │   │   └── Profile.java
    │   │   │   └── repository/
    │   │   │       └── ProfileRepository.java
    │   │   │
    │   │   └── common/
    │   │       ├── GlobalExceptionHandler.java
    │   │       ├── ApiResponse.java
    │   │       ├── PagedResponse.java
    │   │       ├── CorsConfig.java
    │   │       ├── RedisConfig.java
    │   │       └── JacksonConfig.java
    │   │
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db/migration/
    │           ├── V1__create_users.sql
    │           ├── V2__create_content.sql
    │           ├── V3__create_playback.sql
    │           └── V4__create_profiles.sql
    │
    └── test/
        └── java/com/zephyrus/
            ├── auth/
            │   ├── AuthControllerTest.java
            │   ├── AuthServiceTest.java
            │   └── JwtServiceTest.java
            ├── content/
            │   ├── ContentControllerTest.java
            │   └── TmdbClientTest.java
            └── playback/
                └── PlaybackControllerTest.java
```

---

## Phase 1: Foundation & Core MVP

### 1.1 Project Scaffolding

- [ ] Initialize Spring Boot 3.x project (Java 21, Maven)
- [ ] Configure `pom.xml` with all dependencies
- [ ] Set up `application.yml` with profiles (dev/prod)
- [ ] Create `Dockerfile` (multi-stage: Maven build → JRE 21 slim)
- [ ] Create `docker-compose.yml` (PostgreSQL 16 + Redis 7 + API)
- [ ] Create `.env.example` with all required variables
- [ ] Configure `.gitignore`

### 1.2 Common Infrastructure

- [ ] `ApiResponse<T>` — generic response wrapper: `{ success, data, error, meta }`
- [ ] `PagedResponse<T>` — paginated response: adds `page, totalPages, totalItems`
- [ ] `GlobalExceptionHandler` — handles:
  - `MethodArgumentNotValidException` → 400 with field errors
  - `AuthenticationException` → 401
  - `AccessDeniedException` → 403
  - `EntityNotFoundException` → 404
  - `Exception` → 500 generic
- [ ] `CorsConfig` — allow `http://localhost:3000` (dev), configurable for prod
- [ ] `RedisConfig` — `RedisTemplate<String, Object>`, `CacheManager`
- [ ] `JacksonConfig` — Java 8 date/time module, optional snake_case

### 1.3 Authentication Service

#### Entities
- [ ] `User` entity: id (UUID), email (unique), passwordHash, displayName, role (USER/ADMIN), avatarUrl, createdAt, updatedAt, enabled
- [ ] `Role` enum: USER, ADMIN
- [ ] `RefreshToken` entity: id, token (UUID, unique), user (@ManyToOne), expiresAt, revoked, createdAt

#### Security
- [ ] `SecurityConfig`: SecurityFilterChain bean
  - CSRF disabled (stateless API)
  - Session management: STATELESS
  - Public endpoints: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
  - All other endpoints: authenticated
  - Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`
  - `PasswordEncoder` bean (BCrypt)
- [ ] `JwtService`: generate access token (15 min), generate refresh token (7 days), validate, extract claims (userId, email, role)
- [ ] `JwtAuthenticationFilter`: extract Bearer token → validate → set `SecurityContextHolder`

#### Endpoints
- [ ] `POST /api/auth/register` — validate input, check email uniqueness, hash password, create user, return tokens
- [ ] `POST /api/auth/login` — validate credentials, return access + refresh tokens
- [ ] `POST /api/auth/refresh` — validate refresh token, issue new access token
- [ ] `POST /api/auth/logout` — revoke refresh token
- [ ] `GET /api/auth/me` — return current authenticated user info

#### DTOs
- [ ] `RegisterRequest`: email (@Email), password (@Size min=8), displayName (@NotBlank)
- [ ] `LoginRequest`: email, password
- [ ] `AuthResponse`: accessToken, refreshToken, user { id, email, displayName, role, avatarUrl }
- [ ] `RefreshRequest`: refreshToken

### 1.4 Content Service (TMDB Integration)

#### Entities
- [ ] `Title`: id (UUID), tmdbId (int, unique), mediaType (MOVIE/SERIES), title, overview, tagline, releaseDate, runtime, posterPath, backdropPath, voteAverage, popularity, originalLanguage, maturityRating, status
- [ ] `Genre`: id, tmdbId (unique), name
- [ ] Title↔Genre: `@ManyToMany` join table `title_genres`
- [ ] `Season`: id, title (@ManyToOne), seasonNumber, name, overview, posterPath, episodeCount, airDate
- [ ] `Episode`: id, season (@ManyToOne), episodeNumber, name, overview, stillPath, runtime, airDate
- [ ] `CastMember`: id, tmdbPersonId, name, characterName, profilePath, title (@ManyToOne), displayOrder

#### TMDB Client
- [ ] `TmdbClient` using Spring's `RestClient`
  - `getTrending(mediaType, timeWindow, page)` → `/trending/{mediaType}/{timeWindow}`
  - `getPopular(mediaType, page)` → `/{mediaType}/popular`
  - `getDetails(mediaType, tmdbId)` → `/{mediaType}/{id}`
  - `getCredits(mediaType, tmdbId)` → `/{mediaType}/{id}/credits`
  - `getSeasonDetails(tmdbId, seasonNumber)` → `/tv/{id}/season/{num}`
  - `getSimilar(mediaType, tmdbId)` → `/{mediaType}/{id}/similar`
  - `search(query, page)` → `/search/multi`
  - `getGenres(mediaType)` → `/genre/{mediaType}/list`
- [ ] `TmdbCacheService` — Redis cache layer (TTL: 6h for trending, 24h for details)

#### Data Sync
- [ ] `GenreInitializer` — `@EventListener(ApplicationReadyEvent)` seeds genres from TMDB on first boot
- [ ] `ContentSyncJob` — `@Scheduled(cron = "0 0 3 * * *")` nightly sync of trending + popular → local DB

#### Endpoints
- [ ] `GET /api/content/featured` — top 5-8 trending titles for hero billboard
- [ ] `GET /api/content/rows` — pre-assembled content rows (trending, new, per-genre)
- [ ] `GET /api/content?genre=&year=&sort=&page=0&size=20` — paginated catalog
- [ ] `GET /api/content/{id}` — full detail with cast, genres, seasons
- [ ] `GET /api/content/{id}/episodes?season=1` — episodes for a specific season
- [ ] `GET /api/content/{id}/similar` — "More Like This"
- [ ] `GET /api/content/genres` — all genres
- [ ] `GET /api/search?q=&page=0&size=20` — full-text search
- [ ] `GET /api/search/suggestions?q=` — autocomplete (top 5)

### 1.5 Playback Service

#### Entity
- [ ] `WatchProgress`: id, profileId (UUID), titleId (UUID), episodeId (UUID, nullable), progressSeconds (int), durationSeconds (int), lastWatchedAt (Instant), completed (boolean)

#### Endpoints
- [ ] `GET /api/playback/{id}/stream` — returns stream URL (TMDB trailer for demo)
- [ ] `GET /api/playback/{id}/progress` — saved progress for resume
- [ ] `POST /api/playback/{id}/progress` — save progress (debounced client-side, every 10s)
- [ ] `GET /api/playback/continue-watching` — continue watching list

### 1.6 Database Migrations (Flyway)

- [ ] `V1__create_users.sql` — users, refresh_tokens tables
- [ ] `V2__create_content.sql` — titles, genres, title_genres, seasons, episodes, cast_members
- [ ] `V3__create_playback.sql` — watch_progress
- [ ] `V4__create_profiles.sql` — profiles (schema ready for Phase 2)

---

## Phase 2: Personalization & Social

- [ ] Profile CRUD (max 5 per account, kids mode)
- [ ] My List (add/remove/reorder)
- [ ] Watch History (per profile)
- [ ] Recommendation engine (collaborative + content-based filtering)
- [ ] Ratings & reviews endpoints
- [ ] Notification service

## Phase 3: Search & Downloads

- [ ] PostgreSQL full-text search → Meilisearch migration
- [ ] Search analytics (trending searches, recent searches per user)

## Phase 4: Admin Dashboard

- [ ] Analytics endpoints (DAU/MAU, top content, watch time aggregates)
- [ ] Content CMS endpoints (CRUD titles, bulk import, scheduling)
- [ ] User management endpoints (list, ban, suspend)
- [ ] Admin role authorization (`@PreAuthorize("hasRole('ADMIN')")`)

## Phase 5: Premium Features

- [ ] Stripe webhook handler (subscription events)
- [ ] Subscription tier verification middleware
- [ ] Watch Party WebSocket server
- [ ] Parental controls PIN verification

---

## API Reference Summary

### Auth (`/api/auth`)
| Method | Path | Auth | Body |
|---|---|---|---|
| POST | `/register` | Public | `{ email, password, displayName }` |
| POST | `/login` | Public | `{ email, password }` |
| POST | `/refresh` | Public | `{ refreshToken }` |
| POST | `/logout` | Bearer | — |
| GET | `/me` | Bearer | — |

### Content (`/api/content`)
| Method | Path | Auth | Params |
|---|---|---|---|
| GET | `/featured` | Bearer | — |
| GET | `/rows` | Bearer | — |
| GET | `/` | Bearer | `genre, year, sort, page, size` |
| GET | `/{id}` | Bearer | — |
| GET | `/{id}/episodes` | Bearer | `season` |
| GET | `/{id}/similar` | Bearer | — |
| GET | `/genres` | Bearer | — |

### Search (`/api/search`)
| Method | Path | Auth | Params |
|---|---|---|---|
| GET | `/` | Bearer | `q, page, size` |
| GET | `/suggestions` | Bearer | `q` |

### Playback (`/api/playback`)
| Method | Path | Auth | Body/Params |
|---|---|---|---|
| GET | `/{id}/stream` | Bearer | — |
| GET | `/{id}/progress` | Bearer | — |
| POST | `/{id}/progress` | Bearer | `{ progressSeconds, durationSeconds }` |
| GET | `/continue-watching` | Bearer | — |

---

## Environment Variables

```env
# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=zephyrus
POSTGRES_USER=zephyrus
POSTGRES_PASSWORD=your_password_here

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-256-bit-secret-key-here
JWT_ACCESS_EXPIRY=900000        # 15 minutes (ms)
JWT_REFRESH_EXPIRY=604800000    # 7 days (ms)

# TMDB
TMDB_API_KEY=your_tmdb_api_key
TMDB_BASE_URL=https://api.themoviedb.org/3

# OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000

# Server
SERVER_PORT=8080
```

---

## Docker Compose (Local Dev)

```yaml
services:
  postgres:
    image: postgres:16-alpine
    ports: ["5432:5432"]
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  api:
    build: .
    ports: ["8080:8080"]
    depends_on: [postgres, redis]
    env_file: .env

volumes:
  pgdata:
```

---

## Verification

```bash
# Build
./mvnw clean package -DskipTests

# Run tests
./mvnw test

# Start with Docker
docker compose up -d

# Check API health
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html
```
