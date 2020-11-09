package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.OrganismStatusTracking;
import com.dtol.platform.es.repository.OrganismStatusTrackingRepository;
import com.dtol.platform.es.service.OrganismStatusTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

@Service
@Transactional
public class OrganismStatusTrackingServiceImpl implements OrganismStatusTrackingService {

    @Autowired
    OrganismStatusTrackingRepository organismStatusTrackingRepository;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<OrganismStatusTracking> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrganismStatusTracking> pageObj = organismStatusTrackingRepository.findAll(pageable);
        return pageObj.toList();
    }

    @Override
    public long getBiosampleStatusTrackingCount() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .build();
        long count = elasticsearchOperations
                .count(searchQuery, IndexCoordinates.of("statuses"));
        return count;
    }

}
