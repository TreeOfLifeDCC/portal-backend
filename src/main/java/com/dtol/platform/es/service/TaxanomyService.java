package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.RootOrganism;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaxanomyService {

    public String findTaxanomiesByParent(String parent);

    public Boolean findIfTaxanomyHasChild(String organism);

    public String getTaxonomicRanksAndCounts(Optional<String> taxonomy) throws ParseException;

    public String getChildTaxonomyRank(Optional<String> search, Optional<String> filter, String taxonomy, String value, String childRank, String taxaTree, String type) throws ParseException;

    public String getPhylogeneticTree();

    public String phylogeneticTreeSearch(String param);

}
