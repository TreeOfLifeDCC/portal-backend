package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.repository.OrganismRepository;
import com.dtol.platform.es.service.OrganismService;
import org.elasticsearch.index.query.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Service
@Transactional
public class OrganismServiceImpl implements OrganismService {

    @Autowired
    OrganismRepository organismRepository;
    @Value("${ES_CONNECTION_URL}")
    String esConnectionURL;
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

    @Override
    public Organism findBioSampleByAccession(String accession) {
        Organism organism = organismRepository.findBioSampleByAccession(accession);
        return organism;
    }

    @Override
    public String saveBioSample(Organism organism) {
        Organism bs = organismRepository.save(organism);
        return bs.getAccession();
    }

    @Override
    public long getBiosampleCount() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .build();
        long count = elasticsearchOperations
                .count(searchQuery, IndexCoordinates.of("organisms"));
        return count;
    }

    @Override
    public Organism findBioSampleByOrganismByText(String organism) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("organism.text", organism).operator(Operator.AND))
                .build();
        SearchHits<Organism> bioSample = elasticsearchOperations
                .search(searchQuery, Organism.class, IndexCoordinates.of("organisms"));

        if (bioSample.getTotalHits() > 0) {
            return bioSample.getSearchHit(0).getContent();
        } else {
            return new Organism();
        }
    }

}
