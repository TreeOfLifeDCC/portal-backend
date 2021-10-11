package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.DTO.ENAFirstPublicDataResponseDTO;
import com.dtol.platform.es.mapping.DTO.GeoLocationDTO;
import com.dtol.platform.es.mapping.DTO.GeoLocationResponseDTO;
import com.dtol.platform.es.mapping.SecondaryOrganism;
import com.dtol.platform.es.repository.OrganismRepository;
import com.dtol.platform.es.service.OrganismService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.index.query.Operator;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

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
    public List<SecondaryOrganism> findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder) {
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

        Page<SecondaryOrganism> pageObj = organismRepository.findAll(pageable);
        return pageObj.toList();
    }

    @Override
    public SecondaryOrganism findBioSampleByAccession(String accession) {
        SecondaryOrganism secondaryOrganism = organismRepository.findBioSampleByAccession(accession);
        return secondaryOrganism;
    }

    @Override
    public String saveBioSample(SecondaryOrganism secondaryOrganism) {
        SecondaryOrganism bs = organismRepository.save(secondaryOrganism);
        return bs.getAccession();
    }

    @Override
    public long getBiosampleCount() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .build();
        long count = elasticsearchOperations
                .count(searchQuery, IndexCoordinates.of("organisms_test"));
        return count;
    }

    @Override
    public SecondaryOrganism findBioSampleByOrganismByText(String organism) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("organism.text", organism).operator(Operator.AND))
                .build();
        SearchHits<SecondaryOrganism> bioSample = elasticsearchOperations
                .search(searchQuery, SecondaryOrganism.class, IndexCoordinates.of("organisms_test"));

        if (bioSample.getTotalHits() > 0) {
            return bioSample.getSearchHit(0).getContent();
        } else {
            return new SecondaryOrganism();
        }
    }

    @Override
    public Map<String, JSONArray> getSpecimensFilters(String accession) throws ParseException {
        Map<String, JSONArray> filterMap = new HashMap<String, JSONArray>();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'size':0,");
        sb.append("'query' : { 'bool' : { 'should' : [");
        sb.append("{'terms' : {'accession':['");
        sb.append(accession);
        sb.append("']}}]}},");

        sb.append("'aggregations':{");
        sb.append("'filters': { 'nested': { 'path':'specimens'},");
        sb.append("'aggs':{");
        sb.append("'sex_filter':{'terms':{'field':'records.sex', 'size': 2000}},");
        sb.append("'organism_part_filter':{'terms':{'field':'records.organismPart', 'size': 2000}}");
        sb.append("}}}}");
        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/organisms_test/_search", query);
        JSONObject aggregations = (JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations")).get("filters");
        JSONArray sexFilter = (JSONArray) ((JSONObject) aggregations.get("sex_filter")).get("buckets");
        JSONArray orgPartFilterObj = (JSONArray) ((JSONObject) aggregations.get("organism_part_filter")).get("buckets");

        filterMap.put("sex", sexFilter);
        filterMap.put("organismPart", orgPartFilterObj);

        return filterMap;
    }

    @Override
    public String getOrganismByAccession(String accession) {
        Map<String, JSONArray> filterMap = new HashMap<String, JSONArray>();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'size':0,");
        sb.append("'query' : { 'bool' : { 'must' : [");
        sb.append("{'terms' : {'accession.keyword':['");
        sb.append(accession);
        sb.append("']}}]}}}");

        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/organisms_test/_search", query);

        return respString;
    }

    @Override
    public String getSpecimenByAccession(String accession) {
        Map<String, JSONArray> filterMap = new HashMap<String, JSONArray>();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'query' : { 'bool' : { 'must' : [");
        sb.append("{'terms' : {'accession.keyword':['");
        sb.append(accession);
        sb.append("']}}]}}}");

        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/specimens_test/_search", query);

        return respString;
    }

    @Override
    public ArrayList<GeoLocationDTO> getOrganismsLocations() {
        Map<String, JSONArray> filterMap = new HashMap<String, JSONArray>();
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        sb.append("'size':10000,");
        sb.append("'query' : { 'bool' : { 'must' : [");
        sb.append("]}}}");
        ArrayList<GeoLocationDTO> geoLocationsList = new ArrayList<>();

        try {
            String query = sb.toString().replaceAll("'", "\"");
            String respString = this.postRequest("http://" + esConnectionURL + "/geolocation_organism/_search", query);
            ObjectMapper mapper = new ObjectMapper();
            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
            JSONObject hits = (JSONObject) (((JSONObject) new JSONParser().parse(respString)).get("hits"));
            JSONArray data = (JSONArray) (hits.get("hits"));
            data.forEach(dataObject -> {
                JSONObject jObject = (JSONObject) dataObject;
                try {
                    GeoLocationResponseDTO response = mapper.readValue(jObject.get("_source").toString(), GeoLocationResponseDTO.class);
                    if (response != null && (response.getGeographicLocationLongitude() != null && response.getGeographicLocationLatitude() != null)
                            && ((response.getGeographicLocationLongitude().getText() != null && response.getGeographicLocationLatitude().getText() != null))
                            && !("not provided".equalsIgnoreCase(response.getGeographicLocationLongitude().getText()) || "not provided".equalsIgnoreCase(response.getGeographicLocationLatitude().getText()))
                            && !("not collected".equalsIgnoreCase(response.getGeographicLocationLongitude().getText()) || "not collected".equalsIgnoreCase(response.getGeographicLocationLatitude().getText()))) {
                        ArrayList<Double> coordinates = new ArrayList<>();
                        coordinates.add(format.parse(response.getGeographicLocationLongitude().getText()).doubleValue());
                        coordinates.add(format.parse(response.getGeographicLocationLatitude().getText()).doubleValue());
                        geoLocationsList.add(GeoLocationDTO.builder().type("Point").id(response.getId()).coordinates(coordinates).build());
                    }
                } catch (JsonProcessingException | java.text.ParseException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return geoLocationsList;
    }

    @Override
    public Map<String, List<JSONObject>> getCountOrganismParts() {
        Map<String, List<JSONObject>> resultMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        sb.append("{ 'aggregations':{ 'organismPart': {'terms':{'field':'organismPart.keyword'}},");
        sb.append(" 'lifestage': {'terms':{'field':'lifestage.keyword'}},");
        sb.append(" 'habitat': {'terms':{'field':'habitat.keyword'}},");
        sb.append(" 'sex': {'terms':{'field':'sex.keyword'}");
        sb.append("}}}");
        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/organisms_test/_search", query);
        JSONObject aggregations = null;
        try {
            aggregations = (JSONObject) (((JSONObject) new JSONParser().parse(respString)).get("aggregations"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        resultMap.put("habitat", (JSONArray) ((JSONObject) aggregations.get("habitat")).get("buckets"));
        resultMap.put("sex", (JSONArray) ((JSONObject) aggregations.get("sex")).get("buckets"));

        resultMap.put("lifestage", (JSONArray) ((JSONObject) aggregations.get("lifestage")).get("buckets"));

        resultMap.put("organismPart", (JSONArray) ((JSONObject) aggregations.get("organismPart")).get("buckets"));

        return resultMap;
    }

    @Override
    public List<ENAFirstPublicDataResponseDTO> getFirstPublicCount() {
        Map<String, List<JSONObject>> resultMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append("{'size':0,");
        sb.append("'aggregations': {");
        sb.append("'filters': { 'nested': {'path': 'experiment'}, 'aggs': {");
        sb.append("'firstPublic': {'terms': {'field': 'experiment.first_public'}}");

        sb.append("}}}}");
        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/data_portal_index/_search", query);
        JSONObject aggregations = null;
        try {
            aggregations = (JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations")).get("filters");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ArrayList<ENAFirstPublicDataResponseDTO> response = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = "2000-01-01";

        JSONArray data = (JSONArray) ((JSONObject) aggregations.get("firstPublic")).get("buckets");
        ObjectMapper mapper = new ObjectMapper();
        data.forEach(dataObject -> {
            JSONObject jObject = (JSONObject) dataObject;
            try {
                response.add(ENAFirstPublicDataResponseDTO.builder().count((Long) jObject.get("doc_count")).enaFirstPublicInDate(sdf.parse((String) jObject.get("key"))).build());

            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        });
        Collections.sort(response,  (o1, o2) -> o2.getEnaFirstPublicInDate().compareTo(o1.getEnaFirstPublicInDate()));
        response.forEach(data1->{
            data1.setEnaFirstPublic(sdf.format(data1.getEnaFirstPublicInDate()));
        });
        return response;
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
            InputStream st = rs.getEntity().getContent();
            resp = IOUtils.toString(st, StandardCharsets.UTF_8.name());
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
