package com.samson.zephyrus.admin.cms;

import com.samson.zephyrus.admin.cms.dto.*;
import com.samson.zephyrus.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Content CMS", description = "Create, update, import, and schedule streaming content")
public class ContentCmsController {

    private final ContentCmsService cmsService;

    @GetMapping
    @Operation(summary = "List all titles", description = "Paginated title list including unpublished content")
    public ResponseEntity<ApiResponse<Page<AdminTitleDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(cmsService.listTitles(page, size)));
    }

    @PostMapping
    @Operation(summary = "Create title manually")
    public ResponseEntity<ApiResponse<AdminTitleDto>> create(
            @RequestBody @Valid CreateTitleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cmsService.createTitle(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update title")
    public ResponseEntity<ApiResponse<AdminTitleDto>> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateTitleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cmsService.updateTitle(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete title permanently")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        cmsService.deleteTitle(id);
        return ResponseEntity.ok(ApiResponse.success("Title deleted"));
    }

    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publish title", description = "Makes the title visible to all users")
    public ResponseEntity<ApiResponse<AdminTitleDto>> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(cmsService.setPublished(id, true)));
    }

    @PatchMapping("/{id}/unpublish")
    @Operation(summary = "Unpublish title", description = "Hides the title from all users without deleting it")
    public ResponseEntity<ApiResponse<AdminTitleDto>> unpublish(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(cmsService.setPublished(id, false)));
    }

    @PutMapping("/{id}/schedule")
    @Operation(summary = "Schedule availability window", description = "Set availableFrom / availableUntil dates. Null = no restriction.")
    public ResponseEntity<ApiResponse<AdminTitleDto>> schedule(
            @PathVariable UUID id,
            @RequestBody @Valid ScheduleContentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cmsService.scheduleContent(id, request)));
    }

    @PostMapping("/import")
    @Operation(
        summary = "Bulk import from TMDB",
        description = "Deep-imports titles from TMDB: fetches full details, cast, and (for TV) all seasons and episodes"
    )
    public ResponseEntity<ApiResponse<List<AdminTitleDto>>> bulkImport(
            @RequestBody @Valid BulkImportRequest request) {
        log.info("Admin bulk import: {} items", request.getItems().size());
        List<AdminTitleDto> imported = cmsService.bulkImport(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(imported, imported.size() + " title(s) imported"));
    }

    @PostMapping("/import/{tmdbId}")
    @Operation(summary = "Import single title from TMDB")
    public ResponseEntity<ApiResponse<AdminTitleDto>> importOne(
            @PathVariable int tmdbId,
            @RequestParam String mediaType) {
        AdminTitleDto result = cmsService.importFromTmdb(tmdbId, mediaType);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result));
    }
}