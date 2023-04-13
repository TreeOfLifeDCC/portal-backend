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

     String findTaxanomiesByParent(String parent);

     Boolean findIfTaxanomyHasChild(String organism);

     String getTaxonomicRanksAndCounts(Optional<String> taxonomy) throws ParseException;

     String getChildTaxonomyRank(Optional<String> search, Optional<String> filter, String taxonomy, String value, String childRank, String taxaTree, String type) throws ParseException;

     String getPhylogeneticTree();

     String phylogeneticTreeSearch(String param);

     String getPhylogeneticSpecialRankTree();

     String phylogeneticSpecialRankTreeSearch(String param);

}
