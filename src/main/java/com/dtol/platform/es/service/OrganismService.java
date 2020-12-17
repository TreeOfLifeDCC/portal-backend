package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.Organism;

import java.util.List;
import java.util.Optional;

public interface OrganismService {

    public List<Organism> findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder);

    public Organism findBioSampleByAccession(String accession);

    public String saveBioSample(Organism organism);

    public long getBiosampleCount();

    public Organism findBioSampleByOrganismByText(String organism);
}
