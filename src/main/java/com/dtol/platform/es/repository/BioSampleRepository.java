package com.dtol.platform.es.repository;

import com.dtol.platform.es.mapping.BioSample;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BioSampleRepository extends PagingAndSortingRepository<BioSample, String> {

    Page<BioSample> findAll(Pageable pageable);

    BioSample findBioSampleByOrganism(String organism);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"status\": \"?0\"}}]}}")
    List<BioSample> findByStatusUsingCustomQuery(String status);

    @Query("{\"match\": {\"description\": {\"query\": \"?0\"}}}")
    List<BioSample> findByDescription(String description);

    BioSample save(BioSample bioSample);

    BioSample findBioSampleByAccession(String accession);
}
