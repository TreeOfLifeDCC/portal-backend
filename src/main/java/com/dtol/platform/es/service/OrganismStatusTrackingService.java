package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.OrganismStatusTracking;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrganismStatusTrackingService {

    public List<OrganismStatusTracking> findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder);

    public long getBiosampleStatusTrackingCount();

    public Map<String, List<JSONObject>> getFilters();

    public String findSearchResult(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder);

    public String findFilterResults(String filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder);

    public String findBioSampleByOrganismByText(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder);

}
