package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.Organism;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrganismService {

    public List<Organism> findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder);

    public Organism findBioSampleByAccession(String accession);

    public Organism findBioSampleByOrganism(String organism);

    public String saveBioSample(Organism organism);

    public long getBiosampleCount();
}
