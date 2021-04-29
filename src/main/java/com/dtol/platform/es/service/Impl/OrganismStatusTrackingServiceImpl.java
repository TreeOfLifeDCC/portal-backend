package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.SecondaryOrganism;
import com.dtol.platform.es.mapping.StatusTracking;
import com.dtol.platform.es.repository.OrganismStatusTrackingRepository;
import com.dtol.platform.es.service.OrganismStatusTrackingService;
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
public class OrganismStatusTrackingServiceImpl implements OrganismStatusTrackingService {

    @Autowired
    OrganismStatusTrackingRepository organismStatusTrackingRepository;
    @Value("${ES_CONNECTION_URL}")
    String esConnectionURL;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<StatusTracking> findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder) {
        Pageable pageable = null;
        String sortColumnName = "";
        if (sortColumn.isPresent()) {
            sortColumnName = sortColumn.get().toString();
            if (sortColumnName.equals("metadata_submitted_to_biosamples")) {
                sortColumnName = "biosamples";
            } else if (sortColumnName.equals("raw_data_submitted_to_ena")) {
                sortColumnName = "raw_data";
            } else if (sortColumnName.equals("mapped_reads_submitted_to_ena")) {
                sortColumnName = "mapped_reads";
            } else if (sortColumnName.equals("assemblies_submitted_to_ena")) {
                sortColumnName = "assemblies";
            } else if (sortColumnName.equals("annotation_submitted_to_ena")) {
                sortColumnName = "annotation";
            }

            if (sortOrder.get().equals("asc")) {
                pageable = PageRequest.of(page, size, Sort.by(sortColumnName).ascending());

            } else {
                pageable = PageRequest.of(page, size, Sort.by(sortColumnName).descending());
            }
        } else {
            pageable = PageRequest.of(page, size);
        }
        Page<StatusTracking> pageObj = organismStatusTrackingRepository.findAll(pageable);
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

    @Override
    public Map<String, List<JSONObject>> getFilters() {
        Map<String, List<JSONObject>> filterMap = new HashMap<String, List<JSONObject>>();
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withSearchType(SearchType.DEFAULT)
                .addAggregation(terms("biosamples").field("biosamples").size(200))
                .addAggregation(terms("raw_data").field("raw_data").size(200))
                .addAggregation(terms("mapped_reads").field("mapped_reads").size(200))
                .addAggregation(terms("assemblies").field("assemblies").size(200))
                .addAggregation(terms("annotation_complete").field("annotation_complete").size(200))
                .addAggregation(terms("annotation").field("annotation").size(200))
                .build();
        SearchHits<StatusTracking> searchHits = elasticsearchOperations.search(searchQuery, StatusTracking.class,
                IndexCoordinates.of("statuses"));
        Map<String, Aggregation> results = searchHits.getAggregations().asMap();
        ParsedStringTerms bioFilter = (ParsedStringTerms) results.get("biosamples");
        ParsedStringTerms rawFilter = (ParsedStringTerms) results.get("raw_data");
        ParsedStringTerms mappedFilter = (ParsedStringTerms) results.get("mapped_reads");
        ParsedStringTerms assembFilter = (ParsedStringTerms) results.get("assemblies");
        ParsedStringTerms annotCompFilter = (ParsedStringTerms) results.get("annotation_complete");
        ParsedStringTerms annotFilter = (ParsedStringTerms) results.get("annotation");

        filterMap.put("biosamples", bioFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", "Biosamples - " + b.getKeyAsString());
                    filterObj.put("doc_count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));
        filterMap.put("raw_data", rawFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", "Raw data - " + b.getKeyAsString());
                    filterObj.put("doc_count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));
        filterMap.put("mapped_reads", mappedFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", "Mapped reads - " + b.getKeyAsString());
                    filterObj.put("doc_count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));
        filterMap.put("assemblies", assembFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", "Assemblies - " + b.getKeyAsString());
                    filterObj.put("doc_count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));
        filterMap.put("annotation_complete", annotCompFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", "Annotation complete - " + b.getKeyAsString());
                    filterObj.put("doc_count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));
        filterMap.put("annotation", annotFilter.getBuckets()
                .stream()
                .map(b -> {
                    JSONObject filterObj = new JSONObject();
                    filterObj.put("key", "Annotation - " + b.getKeyAsString());
                    filterObj.put("doc_count", b.getDocCount());
                    return filterObj;
                })
                .collect(toList()));

        return filterMap;
    }

    @Override
    public String findFilterResults(Optional<String> filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> taxonomyFilter) throws ParseException {
        List<SecondaryOrganism> results = new ArrayList<SecondaryOrganism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.filterQueryGenerator(filter, from.get(), size.get(), sortColumn, sortOrder, taxonomyFilter);
        respString = this.postRequest("http://" + esConnectionURL + "/statuses/_search", query);

        return respString;
    }

    @Override
    public String findSearchResult(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        List<SecondaryOrganism> results = new ArrayList<SecondaryOrganism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.searchQueryGenerator(search, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://" + esConnectionURL + "/statuses/_search", query);

        return respString;
    }

    @Override
    public String findBioSampleByOrganismByText(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        List<SecondaryOrganism> results = new ArrayList<SecondaryOrganism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getOrganismByText(search, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://" + esConnectionURL + "/organisms/_search", query);

        return respString;
    }

    private StringBuilder getSortQuery(Optional<String> sortColumn, Optional<String> sortOrder) {
        StringBuilder sort = new StringBuilder();
        if (sortColumn.isPresent()) {
            String sortColumnName = "";
            if (sortColumn.isPresent()) {
                sortColumnName = sortColumn.get().toString();
                if (sortColumnName.equals("metadata_submitted_to_biosamples")) {
                    sortColumnName = "biosamples";
                } else if (sortColumnName.equals("raw_data_submitted_to_ena")) {
                    sortColumnName = "raw_data";
                } else if (sortColumnName.equals("mapped_reads_submitted_to_ena")) {
                    sortColumnName = "mapped_reads";
                } else if (sortColumnName.equals("assemblies_submitted_to_ena")) {
                    sortColumnName = "assemblies";
                } else if (sortColumnName.equals("annotation_submitted_to_ena")) {
                    sortColumnName = "annotation";
                }

                sort.append("'sort' : ");
                if (sortOrder.get().equals("asc")) {
                    sort.append("{'" + sortColumnName + "':'asc'},");
                } else {
                    sort.append("{'" + sortColumnName + "':'desc'},");
                }
            }
        }

        return sort;
    }

    private String filterQueryGenerator(Optional<String> filter, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> taxonomyFilter) throws ParseException {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbt = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);

        sb.append("{");
        if (!from.equals("undefined") && !size.equals("undefined"))
            sb.append("'from' :" + from + ",'size':" + size + ",");
        if (sort.length() != 0)
            sb.append(sort);
        sb.append("'query' : { 'bool' : { 'must' : [");

        if (taxonomyFilter.isPresent() && !taxonomyFilter.get().equals("undefined")) {
            String taxArray = taxonomyFilter.get().toString();
            JSONArray taxaTree = (JSONArray) (new JSONParser().parse(taxArray));

            if (taxaTree.size() > 0) {
                sbt.append("{ 'nested' : { 'path': 'taxonomies', 'query' : ");
                sbt.append("{ 'bool' : { 'must' : [");

                for (int i = 0; i < taxaTree.size(); i++) {
                    JSONObject taxa = (JSONObject) taxaTree.get(i);
                    if (taxaTree.size() == 1) {
                        sbt.append("{ 'term' : { 'taxonomies.");
                        sbt.append(taxa.get("rank") + "': '" + taxa.get("taxonomy") + "'}}");
                    } else {
                        if (i == taxaTree.size() - 1) {
                            sbt.append("{ 'term' : { 'taxonomies.");
                            sbt.append(taxa.get("rank") + "': '" + taxa.get("taxonomy") + "'}}");
                        } else {
                            sbt.append("{ 'term' : { 'taxonomies.");
                            sbt.append(taxa.get("rank") + "': '" + taxa.get("taxonomy") + "'}},");
                        }
                    }
                }

                sbt.append("]}}}}");
            }
        }

        if (filter.isPresent() && (!filter.get().equals("undefined") && !filter.get().equals(""))) {
            String[] filterArray = filter.get().split(",");
            sb.append(sbt.toString() + ",");
            for (int i = 0; i < filterArray.length; i++) {
                String[] splitArray = filterArray[i].split("-");

                if (splitArray[0].trim().equals("Biosamples")) {
                    sb.append("{'terms' : {'biosamples':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Raw data")) {
                    sb.append("{'terms' : {'raw_data':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Mapped reads")) {
                    sb.append("{'terms' : {'mapped_reads':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Assemblies")) {
                    sb.append("{'terms' : {'assemblies':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Annotation complete")) {
                    sb.append("{'terms' : {'annotation_complete':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Annotation")) {
                    sb.append("{'terms' : {'annotation':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                }
            }
            sb.append("]}},");
        } else {
            sb.append(sbt.toString());
            sb.append("]}},");
        }

        sb.append("'aggregations': {");
        sb.append("'filters': { 'nested': { 'path':'taxonomies'},");
        sb.append("'aggs':{");
        sb.append("'kingdomRank':{'terms':{'field':'taxonomies.kingdom', 'size': 20000}}");
        if (taxonomyFilter.isPresent() && !taxonomyFilter.get().equals("undefined")) {
            JSONArray taxaTree = (JSONArray) new JSONParser().parse(taxonomyFilter.get().toString());
            if (taxaTree.size() > 0) {
                JSONObject taxa = (JSONObject) taxaTree.get(taxaTree.size() - 1);
                sb.append(",'childRank':{'terms':{'field':'taxonomies." + taxa.get("childRank") + "', 'size': 20000}}");
            }
        }
        sb.append("}},");

        sb.append("'biosamples': {'terms': {'field': 'biosamples'}},");
        sb.append("'raw_data': {'terms': {'field': 'raw_data'}},");
        sb.append("'mapped_reads': {'terms': {'field': 'mapped_reads'}},");
        sb.append("'assemblies': {'terms': {'field': 'assemblies'}},");
        sb.append("'annotation_complete': {'terms': {'field': 'annotation_complete'}},");
        sb.append("'annotation': {'terms': {'field': 'annotation'}}");
        sb.append("}");

        sb.append("}");

        String query = sb.toString().replaceAll("'", "\"").replaceAll(",]", "]");
        return query;
    }

    private String searchQueryGenerator(String search, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);
        StringBuilder searchQuery = new StringBuilder();
        String[] searchArray = search.split(" ");
        for (String temp : searchArray) {
            searchQuery.append("*" + temp + "*");
        }
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
        sb.append("'query' : '" + searchQuery.toString() + "',");
        sb.append("'fields' : ['organism','commonName','biosamples','raw_data','mapped_reads','assemblies','annotation_complete','annotation']");
        sb.append("}},");

        sb.append("'aggregations': {");
        sb.append("'biosamples': {'terms': {'field': 'biosamples'}},");
        sb.append("'raw_data': {'terms': {'field': 'raw_data'}},");
        sb.append("'mapped_reads': {'terms': {'field': 'mapped_reads'}},");
        sb.append("'assemblies': {'terms': {'field': 'assemblies'}},");
        sb.append("'annotation_complete': {'terms': {'field': 'annotation_complete'}},");
        sb.append("'annotation': {'terms': {'field': 'annotation'}}");
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

    private String getOrganismByText(String search, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder) {
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
        sb.append("'match': {");
        sb.append("'organism.text' : '" + search + "'");
        sb.append("}}}");
        String query = sb.toString().replaceAll("'", "\"");
        return query;
    }


}
