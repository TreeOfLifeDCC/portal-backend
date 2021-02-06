package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.DTO.RootOrganism;
import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.mapping.RootSample;
import com.dtol.platform.es.repository.RootOrganismRepository;
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
    @Autowired
    RootOrganismRepository rootOrganismRepository;
    @Value("${ES_CONNECTION_URL}")
    String esConnectionURL;

    @Override
    public List<RootSample> findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder) {
        Pageable pageable = null;
        if (sortColumn.isPresent()) {
            if (sortOrder.get().equals("asc")) {
                pageable = PageRequest.of(page, size, Sort.by(sortColumn.get()).ascending());

            } else {
                pageable = PageRequest.of(page, size, Sort.by(sortColumn.get()).descending());
            }
        } else {
            pageable = PageRequest.of(page, size);
        }

        Page<RootSample> pageObj = rootSampleRepository.findAll(pageable);
        return pageObj.toList();
    }

    @Override
    public List<RootOrganism> findAllOrganisms(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder) {
        Pageable pageable = null;
        if (sortColumn.isPresent()) {
            if (sortOrder.get().equals("asc")) {
                pageable = PageRequest.of(page, size, Sort.by(sortColumn.get() + "").ascending());

            } else {
                pageable = PageRequest.of(page, size, Sort.by(sortColumn.get() + "").descending());
            }
        } else {
            pageable = PageRequest.of(page, size);
        }

        Page<RootOrganism> pageObj = rootOrganismRepository.findAll(pageable);
        return pageObj.toList();
    }

    @Override
    public RootSample findRootSampleByAccession(String accession) {
        RootSample rootSample = rootSampleRepository.findRootSampleByAccession(accession);
        return rootSample;
    }

    @Override
    public Map<String, List<JSONObject>> getRootOrganismFilters() {
        Map<String, List<JSONObject>> filterMap = new HashMap<String, List<JSONObject>>();
        JSONObject sexFilterObj = new JSONObject();
        JSONObject trackFilterObj = new JSONObject();
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withSearchType(SearchType.DEFAULT)
                .addAggregation(terms("trackingSystem").field("trackingSystem").size(100))
                .build();
        SearchHits<RootSample> searchHits = elasticsearchOperations.search(searchQuery, RootSample.class,
                IndexCoordinates.of("new_index"));
        Map<String, Aggregation> results = searchHits.getAggregations().asMap();
        ParsedStringTerms trackFilter = (ParsedStringTerms) results.get("trackingSystem");

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
    public Map<String, JSONArray> getSecondaryOrganismFilters(String organism) throws ParseException {
        Map<String, JSONArray> filterMap = new HashMap<String, JSONArray>();



        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'size':0,");
        sb.append("'query' : { 'bool' : { 'should' : [");
        sb.append("{'terms' : {'organism':['");
        sb.append(organism);
        sb.append("']}}]}},");

        sb.append("'aggregations':{");
        sb.append("'filters': { 'nested': { 'path':'records'},");
        sb.append("'aggs':{");
        sb.append("'sex_filter':{'terms':{'field':'records.sex', 'size': 2000}},");
        sb.append("'tracking_status_filter':{'terms':{'field':'records.trackingSystem', 'size': 2000}},");
        sb.append("'organism_part_filter':{'terms':{'field':'records.organismPart', 'size': 2000}}");
        sb.append("}}}}");
        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/new_index/_search", query);
        JSONObject aggregations = (JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations")).get("filters");
        JSONArray sexFilter = (JSONArray) ((JSONObject) aggregations.get("sex_filter")).get("buckets");
        JSONArray trackFilter = (JSONArray) ((JSONObject) aggregations.get("tracking_status_filter")).get("buckets");
        JSONArray orgPartFilterObj = (JSONArray) ((JSONObject) aggregations.get("organism_part_filter")).get("buckets");

        filterMap.put("sex",sexFilter);
        filterMap.put("trackingSystem",trackFilter);
        filterMap.put("organismPart",orgPartFilterObj);

        return filterMap;

    }

    @Override
    public String findSecondaryOrganismFilterResults(String organism, String filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getSecondaryOrganismFilterResultQuery(organism, filter, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://" + esConnectionURL + "/new_index/_search", query);
        return respString;
    }

    @Override
    public String findRootOrganismFilterResults(String filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getOrganismFilterQuery(filter, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://" + esConnectionURL + "/new_index/_search", query);
        return respString;
    }

    @Override
    public String findRootOrganismSearchResult(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        List<Organism> results = new ArrayList<Organism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getRootOrganismSearchQuery(search, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://" + esConnectionURL + "/new_index/_search", query);

        return respString;
    }

    private StringBuilder getSortQuery(Optional<String> sortColumn, Optional<String> sortOrder) {
        StringBuilder sort = new StringBuilder();
        if (sortColumn.isPresent()) {
            sort.append("'sort' : ");
            if (sortOrder.get().equals("asc")) {
                sort.append("{'" + sortColumn.get() + "':'asc'},");
            } else {
                sort.append("{'" + sortColumn.get() + "':'desc'},");
            }
        }

        return sort;
    }

    private String getSecondaryOrganismFilterResultQuery(String organism, String filter, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder) {
        String[] filterArray = filter.split(",");
        StringBuilder sb = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);

        sb.append("{");
        sb.append("'query' : { 'bool' : { 'should' : [");

        sb.append("{'terms' : {'organism':[");
        sb.append("'"+organism+"'");
        sb.append("]}},");

        sb.append("{'terms' : {'records.trackingSystem':[");
        for (int i = 0; i < filterArray.length; i++) {
            if (i == 0)
                sb.append("'" + filterArray[i] + "'");
            else
                sb.append(",'" + filterArray[i] + "'");
        }
        sb.append("]}},");

        sb.append("{'terms' : {'records.sex':[");
        for (int i = 0; i < filterArray.length; i++) {
            if (i == 0)
                sb.append("'" + filterArray[i] + "'");
            else
                sb.append(",'" + filterArray[i] + "'");
        }
        sb.append("]}}");

        sb.append("]}},");

        sb.append("'aggregations': {");
        sb.append("'filters': { 'nested': {'path': 'records'}, 'aggs': {");
        sb.append("'trackingSystem': {'terms': {'field': 'records.trackingSystem'}},");
        sb.append("'sex': {'terms': {'field': 'records.sex'}}");
        sb.append("}}}}");
        String query = sb.toString().replaceAll("'", "\"");
        return query;
    }

    private String getOrganismFilterQuery(String filter, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder) {
        String[] filterArray = filter.split(",");
        StringBuilder sb = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);

        sb.append("{");
        if (!from.equals("undefined") && !size.equals("undefined"))
            sb.append("'from' :" + from + ",'size':" + size + ",");
        if (sort.length() != 0)
            sb.append(sort);
        sb.append("'query' : { 'bool' : { 'should' : [");

        sb.append("{'terms' : {'trackingSystem':[");
        for (int i = 0; i < filterArray.length; i++) {
            if (i == 0)
                sb.append("'" + filterArray[i] + "'");
            else
                sb.append(",'" + filterArray[i] + "'");
        }
        sb.append("]}}");
        sb.append("]}},");

        sb.append("'aggregations': {");
        sb.append("'trackingSystem': {'terms': {'field': 'trackingSystem'}}");
        sb.append("}");

        sb.append("}");

        String query = sb.toString().replaceAll("'", "\"");
        return query;
    }

    private String getRootOrganismSearchQuery(String search, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);

        sb.append("{");
        if (from.equals("undefined") && size.equals("undefined")) {
            sb.append("'from' :" + 0 + ",'size':" + 20 + ",");
        } else {
            sb.append("'from' :" + from + ",'size':" + size + ",");
        }
        if (sort.length() != 0)
            sb.append(sort);
        sb.append("'query': {");
        sb.append("'query_string': {");
        sb.append("'query' : '*" + search + "*',");
        sb.append("'fields' : ['organism.normalize','commonName.normalize', 'trackingSystem.normalize']");
        sb.append("}},");

        sb.append("'aggregations': {");
        sb.append("'sex': {'terms': {'field': 'sex'}},");
        sb.append("'trackingSystem': {'terms': {'field': 'trackingSystem'}}");
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
    public long getRootOrganismCount() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .build();
        long count = elasticsearchOperations
                .count(searchQuery, IndexCoordinates.of("new_index"));
        return count;
    }

    @Override
    public long getRelatedOrganismCount() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .build();
        long count = elasticsearchOperations
                .count(searchQuery, IndexCoordinates.of("new_index"));
        return count;
    }

    @Override
    public List<RootSample> findRelatedSampleByOrganism(String organism) {
        List<RootSample> rootSample = rootSampleRepository.findRootSampleByOrganism(organism);
        return rootSample;
    }

    @Override
    public String getDistinctRootSamplesByOrganismQuery(String size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> afterKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'size':" + 0 + ",");
        sb.append("'track_total_hits': false,");
        sb.append("'aggs' : { 'group_by_organism' : { 'composite' : {");
        sb.append("'size':" + size + ",");
        sb.append("'sources': [");
        sb.append("{'organism' : {'terms': {'field': 'organism',");
        sb.append("'missing_bucket': true,'order':" + ((sortOrder.isPresent() && sortColumn.get().toString().equals("organism")) ? "'"+sortOrder.get().toString()+"'}}}," : "'asc'}}},"));

        sb.append("{'commonName' : {'terms': {'field': 'commonName',");
        sb.append("'missing_bucket': true,'order':" + ((sortOrder.isPresent() && sortColumn.get().toString().equals("commonName")) ? "'"+ sortOrder.get().toString()+"'}}}," : "'asc'}}},"));

        sb.append("{'sex' : {'terms': {'field': 'sex',");
        sb.append("'missing_bucket': true,'order':" + ((sortOrder.isPresent() && sortColumn.get().toString().equals("sex")) ? "'"+ sortOrder.get().toString()+"'}}}," : "'asc'}}},"));

        sb.append("{'trackingSystem' : {'terms': {'field': 'trackingSystem',");
        sb.append("'missing_bucket': true,'order':" + ((sortOrder.isPresent() && sortColumn.get().toString().equals("trackingSystem")) ? "'"+ sortOrder.get().toString()+"'}}}" : "'asc'}}}"));

        if(afterKey.isPresent())
            sb.append(",'after':"+afterKey.get().toString());
        sb.append("]}}}}");

        String query = sb.toString().replaceAll("'", "\"");
        return query;
    }

    @Override
    public String getDistinctRootSamplesCountByOrganismQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'size':" + 0 + ",");
        sb.append("'aggs' : { 'type_count': { 'cardinality' : { 'field' : 'organism'");
        sb.append("}}}}");
        String query = sb.toString().replaceAll("'", "\"");
        return query;
    }

    @Override
    public JSONObject getDistinctRootSamplesByOrganism(String size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> afterKey) throws ParseException {
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getDistinctRootSamplesByOrganismQuery(size,sortColumn,sortOrder,afterKey);
        respString = this.postRequest("http://" + esConnectionURL + "/root_samples/_search", query);
        JSONObject res = (JSONObject) new JSONParser().parse(respString);
        return res;
    }

    @Override
    public String getDistinctRootSamplesCountByOrganism() throws ParseException {
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getDistinctRootSamplesCountByOrganismQuery();
        respString = this.postRequest("http://" + esConnectionURL + "/root_samples/_search", query);
        JSONObject resp = (JSONObject) new JSONParser().parse(respString);
        String count = ((JSONObject) ((JSONObject) resp.get("aggregations")).get("type_count")).get("value").toString();
        return count;
    }

    @Override
    public String saveRootSample(RootSample rootSample) {
        rootSample.setId(rootSample.getOrganism());
        RootSample bs = rootSampleRepository.save(rootSample);
        return bs.getId();
    }

    @Override
    public RootOrganism findRootSampleByOrganism(String organism) {
        RootOrganism rootOrganism = rootOrganismRepository.findRootOrganismByOrganism(organism);
        return rootOrganism;
    }

    @Override
    public JSONArray findSampleAccessionByOrganism(String organism) throws ParseException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'size':0,");
        sb.append("'query' : { 'bool' : { 'should' : [");
        sb.append("{'terms' : {'organism':['");
        sb.append(organism);
        sb.append("']}}]}},");

        sb.append("'aggs':{");
        sb.append("'accession':{'terms':{'field':'accession'}}");
        sb.append("}}");
        String query = sb.toString().replaceAll("'", "\"");

        String respString = this.postRequest("http://" + esConnectionURL + "/root_samples/_search", query);
        JSONObject aggregations = (JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations"));
        JSONArray accession = (JSONArray) ((JSONObject) aggregations.get("accession")).get("buckets");

        return accession;
    }

}
