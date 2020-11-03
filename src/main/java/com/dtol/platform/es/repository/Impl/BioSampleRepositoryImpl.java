package com.dtol.platform.es.repository.Impl;

import com.dtol.platform.es.mapping.BioSample;
import com.dtol.platform.es.repository.BioSampleRepository;
import org.elasticsearch.index.query.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Repository
public abstract class BioSampleRepositoryImpl implements BioSampleRepository {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public BioSample save(BioSample bioSample) {
        IndexCoordinates indexCoordinates = elasticsearchOperations.getIndexCoordinatesFor(BioSample.class);
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(UUID.randomUUID().toString())
                .withObject(bioSample)
                .build();
        String documentId = elasticsearchOperations.index(indexQuery, indexCoordinates);
        return bioSample;
    }

    @Override
    public BioSample findBioSampleByAccession(String accession) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("accession",accession).operator(Operator.AND))
                .build();
        SearchHits<BioSample> bioSample = elasticsearchOperations
                .search(searchQuery, BioSample.class, IndexCoordinates.of("dtol"));

        if(bioSample.getTotalHits() > 0) {
            return bioSample.getSearchHit(0).getContent();
        }
        else {
            return new BioSample();
        }
    }

    @Override
    public BioSample findBioSampleByOrganism(String organism) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("organism",organism).operator(Operator.AND))
                .build();
        SearchHits<BioSample> bioSample = elasticsearchOperations
                .search(searchQuery, BioSample.class, IndexCoordinates.of("dtol"));

        if(bioSample.getTotalHits() > 0) {
            return bioSample.getSearchHit(0).getContent();
        }
        else {
            return new BioSample();
        }
    }
}
