package com.dtol.platform.es.repository;

import com.dtol.platform.es.mapping.BioSample;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.awt.print.Book;
import java.util.List;

public interface BioSampleRepositoryInterface extends Repository<BioSample, String> {

    BioSample findByName(String name);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"status\": \"?0\"}}]}}")
    List<BioSample> findByStatusUsingCustomQuery(String status);

    @Query("{\"match\": {\"description\": {\"query\": \"?0\"}}}")
    List<BioSample> findByDescription(String description);
}
