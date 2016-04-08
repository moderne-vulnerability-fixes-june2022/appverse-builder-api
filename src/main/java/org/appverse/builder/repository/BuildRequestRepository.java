package org.appverse.builder.repository;

import org.appverse.builder.domain.BuildRequest;
import org.appverse.builder.domain.enumeration.BuildStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

/**
 * Spring Data JPA repository for the BuildRequest entity.
 */
public interface BuildRequestRepository extends JpaRepository<BuildRequest, Long> {


    Optional<BuildRequest> findById(Long id);

    @Query("select case when (count(request) > 0)  then true else false end from BuildRequest request where request.status in :statuses and request.id = :id")
    Boolean isRequestInStatuses(@Param("id") Long id, @Param("statuses") Collection<BuildStatus> statuses);

    Page<BuildRequest> findByChainRequesterLogin(String chainRequesterLogin, Pageable pageable);

}
