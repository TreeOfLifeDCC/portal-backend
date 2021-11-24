package com.dtol.platform.es.repository;

import com.dtol.platform.es.mapping.RootOrganism;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RootOrganismRepository extends PagingAndSortingRepository<RootOrganism, String> {

    Page<RootOrganism> findAll(Pageable pageable);

    RootOrganism findRootOrganismByOrganism(String organism);
}
