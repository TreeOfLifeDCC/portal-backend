package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.RootSample;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RootSampleService {

    public List<RootSample> findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder);

    public RootSample findRootSampleByAccession(String accession);

    public Map<String, List<JSONObject>> getFilters();

    public String findSearchResult(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder);

    public String findFilterResults(String filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder);

    public long getRootSamplesCount();
}
