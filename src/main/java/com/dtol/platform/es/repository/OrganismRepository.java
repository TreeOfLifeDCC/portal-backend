package com.dtol.platform.es.repository;

import com.dtol.platform.es.mapping.Organism;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrganismRepository extends PagingAndSortingRepository<Organism, String> {

    Page<Organism> findAll(Pageable pageable);

    Organism save(Organism organism);

    Organism findBioSampleByAccession(String accession);
}
