package com.dtol.platform.config;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.dtol.platform.es.repository")
@ComponentScan(basePackages = { "com.dtol.platform.es.service" })
public class ElasticSearchConfig {
    @Value("${ES_CONNECTION_URL}")
    String esConnectionURL;

//    @Bean
//    public RestHighLevelClient client() {
//        ClientConfiguration clientConfiguration
//                = ClientConfiguration.builder()
//                .connectedTo(esConnectionURL)
//                .withBasicAuth("elastic","ZG71gVFpLPB27hTF5xS8P3Vi")
//                .build();
//
//        return RestClients.create(clientConfiguration).rest();
//    }

//    @Bean
//    public ElasticsearchOperations elasticsearchTemplate() {
//        return new ElasticsearchRestTemplate(client());
//    }
}
