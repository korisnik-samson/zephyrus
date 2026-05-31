package com.samson.zephyrus.content.repository;

import com.samson.zephyrus.content.model.CastMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CastMemberRepository extends JpaRepository<CastMember, UUID> {

    List<CastMember> findByTitleIdOrderByDisplayOrder(UUID titleId);

    void deleteByTitleId(UUID titleId);
}
