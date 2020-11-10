package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.repository.OrganismRepository;
import com.dtol.platform.es.service.OrganismService;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

@Service
@Transactional
public class OrganismServiceImpl implements OrganismService {

    @Autowired
    OrganismRepository organismRepository;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<Organism> findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder) {
        Pageable pageable = null;
        if (sortColumn.isPresent()) {
            if (sortOrder.get().equals("asc")) {
                if (sortColumn.get().equals("organism")) {
                    pageable = PageRequest.of(page, size, Sort.by("organism.text.keyword").ascending());
                } else {
                    pageable = PageRequest.of(page, size, Sort.by(sortColumn.get() + ".keyword").ascending());
                }

            } else {
                if (sortColumn.get().equals("organism")) {
                    pageable = PageRequest.of(page, size, Sort.by("organism.text.keyword").descending());
                } else {
                    pageable = PageRequest.of(page, size, Sort.by(sortColumn.get() + ".keyword").descending());
                }
            }
        } else {
            pageable = PageRequest.of(page, size);
        }

        Page<Organism> pageObj = organismRepository.findAll(pageable);
        return pageObj.toList();
    }

    public Organism findBioSampleByAccession(String accession) {
        Organism organism = organismRepository.findBioSampleByAccession(accession);
        return organism;
    }

    public Organism findBioSampleByOrganism(String organism) {
        Organism bioSample = organismRepository.findBioSampleByOrganism(organism);
        return bioSample;
    }

    public String saveBioSample(Organism organism) {
        Organism bs = organismRepository.save(organism);
        return bs.getAccession();
    }

    public long getBiosampleCount() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .build();
        long count = elasticsearchOperations
                .count(searchQuery, IndexCoordinates.of("organisms"));
        return count;
    }

    @Override
    public Map<String, JSONObject> getFilterValues() {
        Map<String, JSONObject> filterMap = new HashMap<String, JSONObject>();
        JSONObject sexFilterObj = new JSONObject();
        JSONObject trackFilterObj = new JSONObject();
        JSONObject orgFilterObj = new JSONObject();
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withSearchType(SearchType.DEFAULT)
                .addAggregation(terms("sex").field("sex.keyword"))
                .addAggregation(terms("trackingSystem").field("trackingSystem.keyword"))
                .addAggregation(terms("organismPart").field("organismPart.keyword"))
                .build();
        SearchHits<Organism> searchHits = elasticsearchOperations.search(searchQuery, Organism.class,
                IndexCoordinates.of("organisms"));
        Map<String, Aggregation> results = searchHits.getAggregations().asMap();
        ParsedStringTerms sexFilter = (ParsedStringTerms) results.get("sex");
        ParsedStringTerms trackFilter = (ParsedStringTerms) results.get("trackingSystem");
        ParsedStringTerms orgFilter = (ParsedStringTerms) results.get("organismPart");

        sexFilterObj.put("count", sexFilter.getBuckets().size());
        sexFilterObj.put("filter", sexFilter.getBuckets()
                .stream()
                .map(b -> b.getKeyAsString())
                .collect(toList()));

        trackFilterObj.put("count", trackFilter.getBuckets().size());
        trackFilterObj.put("filter", trackFilter.getBuckets()
                .stream()
                .map(b -> b.getKeyAsString())
                .collect(toList()));

        orgFilterObj.put("count", orgFilter.getBuckets().size());
        orgFilterObj.put("filter", orgFilter.getBuckets()
                .stream()
                .map(b -> b.getKeyAsString())
                .collect(toList()));

        filterMap.put("sex", sexFilterObj);
        filterMap.put("trackingSystem", trackFilterObj);
        filterMap.put("organismPart", orgFilterObj);

        return filterMap;
    }

    @Override
    public HashMap<String, Object> findSearchResult(String search, Optional<String> sortColumn, Optional<String> sortOrder) {
        List<Organism> results = new ArrayList<Organism>();
        long count = 0;
        HashMap<String, Object> response = new HashMap<>();
        FieldSortBuilder sort = null;

        if (sortColumn.isPresent()) {
            if (sortOrder.get().equals("asc")) {
                if (sortColumn.get().equals("organism")) {
                    sort = SortBuilders.fieldSort("organism.text.keyword").order(SortOrder.ASC);
                } else {
                    sort = SortBuilders.fieldSort(sortColumn.get()+".keyword").order(SortOrder.ASC);
                }

            } else {
                if (sortColumn.get().equals("organism")) {
                    sort = SortBuilders.fieldSort("organism.text.keyword").order(SortOrder.DESC);
                } else {
                    sort = SortBuilders.fieldSort(sortColumn.get()+".keyword").order(SortOrder.DESC);
                }
            }
        }
        else {
            sort = SortBuilders.fieldSort("accession.keyword").order(SortOrder.ASC);
        }
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(multiMatchQuery(search)
                        .field("accession")
                        .field("organism")
                        .field("commonName")
                        .field("sex")
                        .field("organismPart")
                        .field("trackingStatus")
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS))
                .withSort(sort)
                .build();
        SearchHits<Organism> organism = elasticsearchOperations
                .search(searchQuery, Organism.class, IndexCoordinates.of("organisms"));

        if (organism.getTotalHits() > 0) {
            count = organism.getTotalHits();
            for (int i = 0; i < count; i++) {
                results.add(organism.getSearchHit(i).getContent());
            }
        }
        response.put("count", count);
        response.put("biosamples", results);
        return response;
    }

}
