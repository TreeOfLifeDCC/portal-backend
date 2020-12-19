package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.mapping.RootSample;
import com.dtol.platform.es.repository.RootSampleRepository;
import com.dtol.platform.es.service.RootSampleService;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;

@Service
@Transactional
public class RootSampleServiceImpl implements RootSampleService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    RootSampleRepository rootSampleRepository;
    @Value("${ES_CONNECTION_URL}")
    String esConnectionURL;

    @Override
    public List<RootSample> findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder) {
        Pageable pageable = null;
        if (sortColumn.isPresent()) {
            if (sortOrder.get().equals("asc")) {
                pageable = PageRequest.of(page, size, Sort.by(sortColumn.get() + ".keyword").ascending());

            } else {
                pageable = PageRequest.of(page, size, Sort.by(sortColumn.get() + ".keyword").descending());
            }
        } else {
            pageable = PageRequest.of(page, size);
        }

        Page<RootSample> pageObj = rootSampleRepository.findAll(pageable);
        return pageObj.toList();
    }

    @Override
    public RootSample findRootSampleByAccession(String accession) {
        RootSample rootSample = rootSampleRepository.findRootSampleByAccession(accession);
        return rootSample;
    }

    @Override
    public Map<String, List<JSONObject>> getFilters() {
        Map<String, List<JSONObject>> filterMap = new HashMap<String, List<JSONObject>>();
        JSONObject sexFilterObj = new JSONObject();
        JSONObject trackFilterObj = new JSONObject();
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withSearchType(SearchType.DEFAULT)
                .addAggregation(terms("sex").field("sex.keyword").size(100))
                .addAggregation(terms("trackingSystem").field("trackingSystem.keyword").size(100))
                .build();
        SearchHits<RootSample> searchHits = elasticsearchOperations.search(searchQuery, RootSample.class,
                IndexCoordinates.of("root_samples"));
        Map<String, Aggregation> results = searchHits.getAggregations().asMap();
        ParsedStringTerms sexFilter = (ParsedStringTerms) results.get("sex");
        ParsedStringTerms trackFilter = (ParsedStringTerms) results.get("trackingSystem");

        filterMap.put("sex", sexFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", b.getKeyAsString());
                    filterObj.put("doc_count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));
        filterMap.put("trackingSystem", trackFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", b.getKeyAsString());
                    filterObj.put("doc_count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));

        return filterMap;
    }

    @Override
    public String findFilterResults(String filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.filterQueryGenerator(filter, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://"+esConnectionURL + "/root_samples/_search", query);
        return respString;
    }

    @Override
    public String findSearchResult(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        List<Organism> results = new ArrayList<Organism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.searchQueryGenerator(search, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://"+esConnectionURL + "/root_samples/_search", query);

        return respString;
    }

    private StringBuilder getSortQuery(Optional<String> sortColumn, Optional<String> sortOrder) {
        StringBuilder sort = new StringBuilder();
        if (sortColumn.isPresent()) {
            sort.append("'sort' : ");
            if (sortOrder.get().equals("asc")) {
                sort.append("{'" + sortColumn.get() + ".keyword':'asc'},");
            } else {
                sort.append("{'" + sortColumn.get() + ".keyword':'desc'},");
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

        sb.append("{'terms' : {'trackingSystem.keyword':[");
        for (int i = 0; i < filterArray.length; i++) {
            if (i == 0)
                sb.append("'" + filterArray[i] + "'");
            else
                sb.append(",'" + filterArray[i] + "'");
        }
        sb.append("]}}");
        sb.append("]}},");

        sb.append("'aggregations': {");
        sb.append("'sex': {'terms': {'field': 'sex.keyword'}},");
        sb.append("'trackingSystem': {'terms': {'field': 'trackingSystem.keyword'}}");
        sb.append("}");

        sb.append("}");

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
        sb.append("'fields' : ['accession','organism','commonName','sex','trackingSystem'],");
        sb.append("'type': 'best_fields',");
        sb.append("'operator': 'OR'");
        sb.append("}},");

        sb.append("'aggregations': {");
        sb.append("'sex': {'terms': {'field': 'sex.keyword'}},");
        sb.append("'trackingSystem': {'terms': {'field': 'trackingSystem.keyword'}}");
        sb.append("}");

        sb.append("}");

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

    @Override
    public long getRootSamplesCount() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .build();
        long count = elasticsearchOperations
                .count(searchQuery, IndexCoordinates.of("root_samples"));
        return count;
    }

    @Override
    public RootSample findRootSampleByOrganism(String organism) {
        RootSample rootSample = rootSampleRepository.findRootSampleByOrganism(organism);
        return rootSample;
    }
}
