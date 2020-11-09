package com.dtol.platform.es.repository;

import com.dtol.platform.es.mapping.Organism;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface OrganismRepository extends PagingAndSortingRepository<Organism, String> {

    Page<Organism> findAll(Pageable pageable);

    Organism findBioSampleByOrganism(String organism);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"status\": \"?0\"}}]}}")
    List<Organism> findByStatusUsingCustomQuery(String status);

    @Query("{\"match\": {\"description\": {\"query\": \"?0\"}}}")
    List<Organism> findByDescription(String description);

    Organism save(Organism organism);

    Organism findBioSampleByAccession(String accession);
}
