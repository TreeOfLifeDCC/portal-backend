package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.RootOrganism;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaxanomyService {

    public String getAllTaxonomiesByType(String type);

    public String findTaxanomiesByParent(String parent);

    public Boolean findIfTaxanomyHasChild(String organism);

    public String getTaxonomicRanksAndCounts(Optional<String> taxonomy) throws ParseException;

    public String getChildTaxonomyRank(Optional<String> filter, String taxonomy, String value, String childRank) throws ParseException;

}
