package com.dtol.platform.es.repository.Impl;

import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.mapping.RootSample;
import com.dtol.platform.es.repository.RootSampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public abstract class RootSampleRepositoryImpl implements RootSampleRepository {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;


    @Override
    public RootSample save(RootSample rootSample) {
        IndexCoordinates indexCoordinates = elasticsearchOperations.getIndexCoordinatesFor(RootSample.class);
        IndexQuery indexQuery = new IndexQueryBuilder()
//                .withId(rootSample.getOrganism())
                .withObject(rootSample)
                .build();
        String documentId = elasticsearchOperations.index(indexQuery, indexCoordinates);
        System.out.println(documentId);
        return rootSample;
    }
}
