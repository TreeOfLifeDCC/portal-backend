package com.dtol.platform.es.repository;

import com.dtol.platform.es.mapping.BioSample;
import org.elasticsearch.index.query.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

import static org.elasticsearch.action.admin.indices.stats.CommonStatsFlags.Flag.Get;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Repository
public class BioSampleRepository {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public String save(BioSample bioSample) {
        IndexCoordinates indexCoordinates = elasticsearchOperations.getIndexCoordinatesFor(BioSample.class);
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(UUID.randomUUID().toString())
                .withObject(bioSample)
                .build();
        String documentId = elasticsearchOperations.index(indexQuery, indexCoordinates);
        return documentId;
    }

    public BioSample findById(String id) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("id",id).operator(Operator.AND))
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
