package com.samson.zephyrus.search;

import com.samson.zephyrus.content.model.Title;
import com.samson.zephyrus.content.repository.TitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Keeps the Meilisearch index in sync with the titles table.
 *
 * <ul>
 *   <li>On startup: full re-index (skipped if Meilisearch is disabled)</li>
 *   <li>Nightly at 04:00: re-index after {@link com.samson.zephyrus.content.service.ContentSyncJob}
 *       runs at 03:00 and populates new titles</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MeilisearchIndexJob {

    private final MeilisearchSyncService meilisearchSyncService;
    private final TitleRepository titleRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void indexOnStartup() {
        if (!meilisearchSyncService.isAvailable()) {
            log.debug("Meilisearch disabled — skipping startup index");
            return;
        }
        log.info("Meilisearch startup index starting…");
        new Thread(this::reindexAll, "meili-startup-index").start();
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void indexNightly() {
        if (!meilisearchSyncService.isAvailable()) return;
        log.info("Meilisearch nightly re-index starting…");
        reindexAll();
    }

    @Transactional(readOnly = true)
    public void reindexAll() {
        try {
            List<Title> titles = titleRepository.findAllWithGenres();
            meilisearchSyncService.indexTitles(titles);
            log.info("Meilisearch index complete — {} titles", titles.size());
        } catch (Exception e) {
            log.error("Meilisearch index failed: {}", e.getMessage());
        }
    }
}