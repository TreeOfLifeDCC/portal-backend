package com.dtol.platform.es.repository;

import com.dtol.platform.es.mapping.BioSampleStatusTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BioSampleStatusTrackingRepository extends PagingAndSortingRepository<BioSampleStatusTracking, String> {

    Page<BioSampleStatusTracking> findAll(Pageable pageable);
}
