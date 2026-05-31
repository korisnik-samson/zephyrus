package com.samson.zephyrus.content.repository;

import com.samson.zephyrus.content.model.ContentRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentRowRepository extends JpaRepository<ContentRow, UUID> {

    List<ContentRow> findAllByOrderBySortOrder();
}
