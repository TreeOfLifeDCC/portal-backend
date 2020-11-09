package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.Organism;

import java.util.List;

public interface OrganismService {

    public List<Organism> findAll(int page, int size);

    public Organism findBioSampleByAccession(String accession);

    public Organism findBioSampleByOrganism(String organism);

    public String saveBioSample(Organism organism);

    public long getBiosampleCount();
}
