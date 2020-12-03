package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.repository.OrganismRepository;
import com.dtol.platform.es.service.OrganismService;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.json.simple.JSONObject;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

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
    public Map<String, List<JSONObject>> getFilters() {
        Map<String, List<JSONObject>> filterMap = new HashMap<String, List<JSONObject>>();
        JSONObject sexFilterObj = new JSONObject();
        JSONObject trackFilterObj = new JSONObject();
        JSONObject orgFilterObj = new JSONObject();
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withSearchType(SearchType.DEFAULT)
                .addAggregation(terms("sex").field("sex.keyword").size(100))
                .addAggregation(terms("trackingSystem").field("trackingSystem.keyword").size(100))
                .addAggregation(terms("organismPart").field("organismPart.keyword").size(100))
                .build();
        SearchHits<Organism> searchHits = elasticsearchOperations.search(searchQuery, Organism.class,
                IndexCoordinates.of("organisms"));
        Map<String, Aggregation> results = searchHits.getAggregations().asMap();
        ParsedStringTerms sexFilter = (ParsedStringTerms) results.get("sex");
        ParsedStringTerms trackFilter = (ParsedStringTerms) results.get("trackingSystem");
        ParsedStringTerms orgFilter = (ParsedStringTerms) results.get("organismPart");

        filterMap.put("sex", sexFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", b.getKeyAsString());
                    filterObj.put("count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));
        filterMap.put("trackingSystem", trackFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", b.getKeyAsString());
                    filterObj.put("count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));
        filterMap.put("organismPart", orgFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", b.getKeyAsString());
                    filterObj.put("count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));

        return filterMap;
    }

    @Override
    public String findFilterResults(String filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        List<Organism> results = new ArrayList<Organism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.filterQueryGenerator(filter, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://"+esConnectionURL + "/organisms/_search", query);

        return respString;
    }

    @Override
    public String findSearchResult(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        List<Organism> results = new ArrayList<Organism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.searchQueryGenerator(search, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://"+esConnectionURL + "/organisms/_search", query);

        return respString;
    }

    private StringBuilder getSortQuery(Optional<String> sortColumn, Optional<String> sortOrder) {
        StringBuilder sort = new StringBuilder();
        if (sortColumn.isPresent()) {
            sort.append("'sort' : ");
            if (sortOrder.get().equals("asc")) {
                if (sortColumn.get().equals("organism")) {
                    sort.append("{'organism.text.keyword':'asc'},");
                } else {
                    sort.append("{'" + sortColumn.get() + ".keyword':'asc'},");
                }
            } else {
                if (sortColumn.get().equals("organism")) {
                    sort.append("{'organism.text.keyword':'desc'},");
                } else {
                    sort.append("{'" + sortColumn.get() + ".keyword':'desc'},");
                }
            }
        }

        return sort;
    }

    private String filterQueryGenerator(String filter, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder) {
        String[] filterArray = filter.split(",");
        StringBuilder sb = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);

        sb.append("{");
        if (!from.equals("undefined") && !size.equals("undefined"))
            sb.append("'from' :" + from + ",'size':" + size + ",");
        if (sort.length() != 0)
            sb.append(sort);
        sb.append("'query' : { 'bool' : { 'should' : [");

        sb.append("{'terms' : {'sex.keyword':[");
        for (int i = 0; i < filterArray.length; i++) {
            if (i == 0)
                sb.append("'" + filterArray[i] + "'");
            else
                sb.append(",'" + filterArray[i] + "'");
        }
        sb.append("]}},");

        sb.append("{'terms' : {'organismPart.keyword':[");
        for (int i = 0; i < filterArray.length; i++) {
            if (i == 0)
                sb.append("'" + filterArray[i] + "'");
            else
                sb.append(",'" + filterArray[i] + "'");
        }
        sb.append("]}},");

        sb.append("{'terms' : {'trackingSystem.keyword':[");
        for (int i = 0; i < filterArray.length; i++) {
            if (i == 0)
                sb.append("'" + filterArray[i] + "'");
            else
                sb.append(",'" + filterArray[i] + "'");
        }
        sb.append("]}}");
        sb.append("]}}}");

        String query = sb.toString().replaceAll("'", "\"");
        return query;
    }

    private String searchQueryGenerator(String search, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);

        sb.append("{");
        if (from.equals("undefined") && size.equals("undefined")) {
            sb.append("'from' :" + 0 + ",'size':" + 20 + ",");
        }
        else {
            sb.append("'from' :" + from + ",'size':" + size + ",");
        }
        if (sort.length() != 0)
            sb.append(sort);
        sb.append("'query': {");
        sb.append("'multi_match': {");
        sb.append("'query' : '"+search+"',");
        sb.append("'fields' : ['accession','commonName','organism.text','organismPart','sex','trackingSystem'],");
        sb.append("'type': 'best_fields',");
        sb.append("'operator': 'OR'");
        sb.append("}}}");
        String query = sb.toString().replaceAll("'", "\"");
        return query;
    }

    private String postRequest(String baseURL, String body) {
        CloseableHttpClient client = HttpClients.createDefault();
        StringEntity entity = null;
        String resp = "";
        try {
            HttpPost httpPost = new HttpPost(baseURL);
            entity = new StringEntity(body);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse rs = client.execute(httpPost);
            resp = IOUtils.toString(rs.getEntity().getContent(), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resp;
    }

}
