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

    public String saveBioSample(Organism organism);

    public long getBiosampleCount();

    public Map<String, List<JSONObject>> getFilters();

    public HashMap<String, Object> findSearchResult(String search, Optional<String> sortColumn, Optional<String> sortOrder);

    public String findFilterResults(String filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder);

    public Organism findBioSampleByOrganismByText(String organism);
}
