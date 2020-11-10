package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.repository.OrganismRepository;
import com.dtol.platform.es.service.OrganismService;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.json.simple.JSONObject;
import org.junit.internal.requests.SortingRequest;
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
        if(sortColumn.isPresent()) {
            if (sortOrder.get().equals("asc")) {
                pageable = PageRequest.of(page, size,Sort.by(sortColumn.get()+".keyword").ascending());
            }
            else {
                pageable = PageRequest.of(page, size,Sort.by(sortColumn.get()+".keyword").descending());
            }
        }
        else {
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

}
