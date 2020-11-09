package com.dtol.platform.es.repository;

import com.dtol.platform.es.mapping.OrganismStatusTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrganismStatusTrackingRepository extends PagingAndSortingRepository<OrganismStatusTracking, String> {

    Page<OrganismStatusTracking> findAll(Pageable pageable);
}
