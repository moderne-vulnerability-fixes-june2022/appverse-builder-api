package org.appverse.builder.repository;

import org.appverse.builder.domain.BuildChain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the BuildChain entity.
 */
public interface BuildChainRepository extends JpaRepository<BuildChain, Long> {

    List<BuildChain> findAll();

    Page<BuildChain> findByRequesterLogin(String requesterLogin, Pageable pageable);

}
