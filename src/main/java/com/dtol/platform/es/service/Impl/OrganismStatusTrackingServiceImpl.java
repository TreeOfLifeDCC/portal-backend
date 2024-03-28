package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.SecondaryOrganism;
import com.dtol.platform.es.repository.OrganismStatusTrackingRepository;
import com.dtol.platform.es.service.OrganismStatusTrackingService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

@Service
@Transactional
public class OrganismStatusTrackingServiceImpl implements OrganismStatusTrackingService {

    @Autowired
    OrganismStatusTrackingRepository organismStatusTrackingRepository;
    @Value("${ES_CONNECTION_URL}")
    String esConnectionURL;

    @Value("${ES_USERNAME}")
    String esUsername;

    @Value("${ES_PASSWORD}")
    String esPassword;

    static final String [] taxaRankArray = {"superkingdom", "kingdom","subkingdom","superphylum","phylum","subphylum","superclass","class","subclass","infraclass","cohort","subcohort","superorder","order","suborder","infraorder","parvorder","section","subsection","superfamily","family","subfamily","tribe","subtribe","genus","series","subgenus","species_group","species_subgroup","species","subspecies","varietas","forma"};

    @Override
    public JSONArray findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder) throws ParseException {
        StringBuilder sb = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);

        sb.append("{");
        sb.append("'from' :" + page + ",'size':" + size + ",");
        if (sort.length() != 0)
            sb.append(sort);
        sb.append("'query' : { 'match_all' : {}}");
        sb.append("}");

        String query = sb.toString().replaceAll("'", "\"");

        String respString = this.postRequest( esConnectionURL + "/tracking_status_index/_search", query);

        JSONArray respArray = (JSONArray) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("hits")).get("hits");
        return respArray;
    }

    @Override
    public long getBiosampleStatusTrackingCount() throws ParseException {

        String respString = this.getRequest( esConnectionURL + "/tracking_status_index/_count");

        JSONObject resp = (JSONObject) new JSONParser().parse(respString);
        long count = Long.valueOf(resp.get("count").toString());
        return count;
    }

    @Override
    public Map<String, List<JSONObject>> getFilters() throws ParseException {
        Map<String, List<JSONObject>> filterMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append("{'size':0, 'aggregations':{");
        sb.append("'trackingSystem': { 'nested': { 'path':'trackingSystem'},");
        sb.append("'aggs':{");
        sb.append("'rank':{'terms':{'field':'trackingSystem.rank', 'order': { '_key' : 'desc' }},");
        sb.append("'aggregations':{ 'name': {'terms':{'field':'trackingSystem.name'},");
        sb.append("'aggregations':{ 'status': {'terms':{'field':'trackingSystem.status'}");
        sb.append("}}}}}}},");
        sb.append("'symbionts_biosamples_status': {'terms': {'field': 'symbionts_biosamples_status'}},");
        sb.append("'symbionts_raw_data_status': {'terms': {'field': 'symbionts_raw_data_status'}},");
        sb.append("'symbionts_assemblies_status': {'terms': {'field': 'symbionts_assemblies_status'}}");
        sb.append("}}");
        String query = sb.toString().replaceAll("'", "\"");

        String respString = this.postRequest( esConnectionURL + "/tracking_status_index/_search", query);
        JSONObject aggregations = (JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations")).get("trackingSystem")).get("rank");
        JSONArray trackFilterArray = (JSONArray) (aggregations.get("buckets"));
        for(int i=0; i<trackFilterArray.size();i++) {
            JSONObject obj = (JSONObject) trackFilterArray.get(i);
            JSONObject trackObj = (JSONObject) ((JSONArray) ((JSONObject) (obj).get("name")).get("buckets")).get(0);
            String name = "";
            if (trackObj.get("key").toString().equals("biosamples")) {
                name = "Biosamples";
            } else if (trackObj.get("key").toString().equals("mapped_reads")) {
                name = "Mapped reads";
            } else if (trackObj.get("key").toString().equals("assemblies")) {
                name = "Assemblies";
            } else if (trackObj.get("key").toString().equals("raw_data")) {
                name = "Raw data";
            } else if (trackObj.get("key").toString().equals("annotation")) {
                name = "Annotation";
            } else if (trackObj.get("key").toString().equals("annotation_complete")) {
                name = "Annotation complete";
            }

            JSONArray arr = (JSONArray) (((JSONObject) trackObj.get("status"))).get("buckets");
            List<JSONObject> statusArray = new ArrayList<JSONObject>();
            for (int j = 0; j < arr.size(); j++) {
                JSONObject temp = (JSONObject) arr.get(j);
                JSONObject filterObj = new JSONObject();
                filterObj.put("key", name + " - " + temp.get("key"));
                filterObj.put("doc_count", temp.get("doc_count"));
                statusArray.add(filterObj);
            }
            filterMap.put(trackObj.get("key").toString(), statusArray);
        }

        // symbionts
        List<JSONObject> statusArr = new ArrayList<JSONObject>();
        JSONObject mainsAgg = (JSONObject)(( ((JSONObject) new JSONParser().parse(respString)).get("aggregations")));

        // symbionts
        JSONArray symbionts_biosamples_arr = (JSONArray)((JSONObject) mainsAgg.get("symbionts_biosamples_status")).get("buckets");
        if (!symbionts_biosamples_arr.isEmpty()) {
            JSONObject symbiontsBioSamplesStatus = (JSONObject)symbionts_biosamples_arr.get(0);
            statusArr.add(symbiontsBioSamplesStatus);
            filterMap.put("symbionts_biosamples_status", new ArrayList<JSONObject>(Arrays.asList(symbiontsBioSamplesStatus)));
        }

        JSONArray symbionts_rawdata_arr = (JSONArray)((JSONObject) mainsAgg.get("symbionts_raw_data_status")).get("buckets");
        if (!symbionts_rawdata_arr.isEmpty()) {
            JSONObject symbiontsRawDataStatus = (JSONObject)symbionts_rawdata_arr.get(0);
            statusArr.add(symbiontsRawDataStatus);
            filterMap.put("symbionts_raw_data_status", new ArrayList<JSONObject>(Arrays.asList(symbiontsRawDataStatus)));
        }

        JSONArray symbionts_assemblies_arr = (JSONArray)((JSONObject) mainsAgg.get("symbionts_assemblies_status")).get("buckets");
        if (!symbionts_assemblies_arr.isEmpty()) {
            JSONObject symbiontsAssembliesStatus = (JSONObject)symbionts_assemblies_arr.get(0);
            statusArr.add(symbiontsAssembliesStatus);
            filterMap.put("symbionts_assemblies_status", new ArrayList<JSONObject>(Arrays.asList(symbiontsAssembliesStatus)));
        }

        return filterMap;
    }

    @Override
    public String findFilterResults(Optional<String> search, Optional<String> filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> taxonomyFilter) throws ParseException {
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.filterQueryGenerator(search, filter, from.get(), size.get(), sortColumn, sortOrder, taxonomyFilter);

        respString = this.postRequest( esConnectionURL + "/tracking_status_index/_search", query);

        return respString;
    }

    @Override
    public String findSearchResult(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        List<SecondaryOrganism> results = new ArrayList<SecondaryOrganism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.searchQueryGenerator(search, from.get(), size.get(), sortColumn, sortOrder);

        respString = this.postRequest( esConnectionURL + "/tracking_status_index/_search", query);


        return respString;
    }

    @Override
    public String findBioSampleByOrganismByText(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        List<SecondaryOrganism> results = new ArrayList<SecondaryOrganism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getOrganismByText(search, from.get(), size.get(), sortColumn, sortOrder);

        respString = this.postRequest( esConnectionURL + "/organisms/_search", query);

        return respString;
    }

    private StringBuilder getSortQuery(Optional<String> sortColumn, Optional<String> sortOrder) {
        StringBuilder sort = new StringBuilder();
        sort.append("'sort' : [");
        if (sortColumn.isPresent()) {
            String sortColumnName = "";
            if (sortColumn.isPresent()) {
                sortColumnName = sortColumn.get().toString();
                if (sortColumnName.equals("metadata_submitted_to_biosamples")) {
                    sortColumnName = "biosamples.keyword";
                } else if (sortColumnName.equals("raw_data_submitted_to_ena")) {
                    sortColumnName = "raw_data.keyword";
                } else if (sortColumnName.equals("mapped_reads_submitted_to_ena")) {
                    sortColumnName = "mapped_reads.keyword";
                } else if (sortColumnName.equals("assemblies_submitted_to_ena")) {
                    sortColumnName = "assemblies.keyword";
                } else if (sortColumnName.equals("annotation_submitted_to_ena")) {
                    sortColumnName = "annotation.keyword";
                } else if (sortColumnName.equals("annotation_complete")) {
                    sortColumnName = "annotation_complete.keyword";
                }

                if (sortOrder.get().equals("asc")) {
                    sort.append("{'" + sortColumnName + "':'asc'}");
                } else {
                    sort.append("{'" + sortColumnName + "':'desc'}");
                }
            }
        }
        else {

            sort.append("{'trackingSystem.rank':{'order':'desc','nested':{'path': 'trackingSystem', 'filter': {'term': {'trackingSystem.status': 'Done'}}}}}");

        }

        sort.append("],");
        return sort;
    }

    private String filterQueryGenerator(Optional<String> search, Optional<String> filter, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> taxonomyFilter) throws ParseException {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbt = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);
        Boolean isPhylogenyFilter = false;
        String phylogenyRank = "";
        String phylogenyTaxId = "";
        StringBuilder searchQuery = new StringBuilder();

        if(search.isPresent()) {
            String[] searchArray = search.get().split(" ");
            for (String temp : searchArray) {
                searchQuery.append("*" + temp + "*");
            }
        }
        sb.append("{");
        if (!from.equals("undefined") && !size.equals("undefined"))
            sb.append("'from' :" + from + ",'size':" + size + ",");
        if (sort.length() != 0)
            sb.append(sort);
        sb.append("'query' : { 'bool' : { 'must' : [");

        if (searchQuery.length() != 0) {
            sb.append("{'multi_match': {");
            sb.append("'operator': 'AND',");
            sb.append("'query' : '" + searchQuery.toString() + "',");
            sb.append("'fields' : ['organism.autocomp', 'commonName.autocomp', 'biosamples.autocomp','raw_data.autocomp'," +
                    "'mapped_reads.autocomp'," +
                    "'assemblies.autocomp','annotation_complete.autocomp','annotation.autocomp', " +
                    "'symbionts_records.organism.text.autocomp']");
            sb.append("}},");
        }

        if (taxonomyFilter.isPresent() && !taxonomyFilter.get().equals("undefined")) {
            String taxArray = taxonomyFilter.get().toString();
            JSONArray taxaTree = (JSONArray) (new JSONParser().parse(taxArray));
            if (taxaTree.size() > 0) {
                for (int i = 0; i < taxaTree.size(); i++) {
                    JSONObject taxa = (JSONObject) taxaTree.get(i);
                    if (taxaTree.size() == 1) {
                        sbt.append("{ 'nested' : { 'path': 'taxonomies', 'query' : ");
                        sbt.append("{ 'nested' : { 'path': 'taxonomies."+taxa.get("rank")+"', 'query' : ");
                        sbt.append("{ 'bool' : { 'must' : [");
                        sbt.append("{ 'term' : { 'taxonomies.");
                        sbt.append(taxa.get("rank") + ".scientificName': '" + taxa.get("taxonomy") + "'}}");
                        sbt.append("]}}}}}}");
                    } else {
                        if (i == taxaTree.size() - 1) {
                            sbt.append("{ 'nested' : { 'path': 'taxonomies', 'query' : ");
                            sbt.append("{ 'nested' : { 'path': 'taxonomies."+taxa.get("rank")+"', 'query' : ");
                            sbt.append("{ 'bool' : { 'must' : [");
                            sbt.append("{ 'term' : { 'taxonomies.");
                            sbt.append(taxa.get("rank") + ".scientificName': '" + taxa.get("taxonomy") + "'}}");
                            sbt.append("]}}}}}}");
                        } else {
                            sbt.append("{ 'nested' : { 'path': 'taxonomies', 'query' : ");
                            sbt.append("{ 'nested' : { 'path': 'taxonomies."+taxa.get("rank")+"', 'query' : ");
                            sbt.append("{ 'bool' : { 'must' : [");
                            sbt.append("{ 'term' : { 'taxonomies.");
                            sbt.append(taxa.get("rank") + ".scientificName': '" + taxa.get("taxonomy") + "'}}");
                            sbt.append("]}}}}}},");
                        }
                    }
                }
            }
        }

        if (filter.isPresent() && (!filter.get().equals("undefined") && !filter.get().equals(""))) {
            String[] filterArray = filter.get().split(",");
            for (int i = 0; i < filterArray.length; i++) {
                String[] splitArray = filterArray[i].split("-");

                if (splitArray[0].trim().equals("Biosamples")) {
                    sb.append("{'terms' : {'biosamples.keyword':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Raw data")) {
                    sb.append("{'terms' : {'raw_data.keyword':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Mapped reads")) {
                    sb.append("{'terms' : {'mapped_reads.keyword':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Assemblies")) {
                    sb.append("{'terms' : {'assemblies.keyword':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Annotation complete")) {
                    sb.append("{'terms' : {'annotation_complete.keyword':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Annotation")) {
                    sb.append("{'terms' : {'annotation.keyword':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                }else if (splitArray[0].trim().equals("symbiontsBioSamplesStatus")) {
                    String symbiontsStatusFilter = filterArray[i].trim().replaceFirst("symbiontsBioSamplesStatus-", "");
                    sb.append("{'terms' : {'symbionts_biosamples_status':[");
                    sb.append("'" + symbiontsStatusFilter.trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("symbiontsRawDataStatus")) {
                    String symbiontsStatusFilter = filterArray[i].trim().replaceFirst("symbiontsRawDataStatus-", "");
                    sb.append("{'terms' : {'symbionts_raw_data_status':[");
                    sb.append("'" + symbiontsStatusFilter.trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("symbiontsAssembliesStatus")) {
                    String symbiontsStatusFilter = filterArray[i].trim().replaceFirst("symbiontsAssembliesStatus-", "");
                    sb.append("{'terms' : {'symbionts_assemblies_status':[");
                    sb.append("'" + symbiontsStatusFilter.trim() + "'");
                    sb.append("]}},");
                }else if (Arrays.asList(taxaRankArray).contains(splitArray[0].trim())) {
                    isPhylogenyFilter = true;
                    phylogenyRank = splitArray[0].trim();
                    phylogenyTaxId = splitArray[1].trim();
                    sb.append("{ 'nested' : { 'path': 'taxonomies', 'query' : ");
                    sb.append("{ 'nested' : { 'path': 'taxonomies."+splitArray[0].trim()+"', 'query' : ");
                    sb.append("{ 'bool' : { 'must' : [");
                    sb.append("{ 'term' : { 'taxonomies.");
                    sb.append(splitArray[0].trim() + ".tax_id': '" + splitArray[1].trim() + "'}}");
                    sb.append("]}}}}}},");
                }
            }
            sb.append("]}},");
        } else {
            sb.append(sbt.toString());
            sb.append("]}},");
        }

        sb.append("'aggregations': {");
        sb.append("'symbionts_biosamples_status': {'terms': {'field': 'symbionts_biosamples_status'}},");
        sb.append("'symbionts_raw_data_status': {'terms': {'field': 'symbionts_raw_data_status'}},");
        sb.append("'symbionts_assemblies_status': {'terms': {'field': 'symbionts_assemblies_status'}},");
        sb.append("'kingdomRank': { 'nested': { 'path':'taxonomies.kingdom'},");
        sb.append("'aggs':{'scientificName':{'terms':{'field':'taxonomies.kingdom.scientificName', 'size': 20000},");
        sb.append("'aggs':{'commonName':{'terms':{'field':'taxonomies.kingdom.commonName', 'size': 20000}},");
        sb.append("'taxId':{'terms':{'field':'taxonomies.kingdom.tax_id.keyword', 'size': 20000}}}}}},");
        if (taxonomyFilter.isPresent() && !taxonomyFilter.get().equals("undefined") && !isPhylogenyFilter) {
            JSONArray taxaTree = (JSONArray) new JSONParser().parse(taxonomyFilter.get().toString());
            if (taxaTree.size() > 0) {
                JSONObject taxa = (JSONObject) taxaTree.get(taxaTree.size() - 1);
                sb.append("'childRank': { 'nested': { 'path':'taxonomies."+ taxa.get("childRank")+"'},");
                sb.append("'aggs':{'scientificName':{'terms':{'field':'taxonomies."+taxa.get("childRank")+".scientificName', 'size': 20000},");
                sb.append("'aggs':{'commonName':{'terms':{'field':'taxonomies."+taxa.get("childRank")+".commonName', 'size': 20000}},");
                sb.append("'taxId':{'terms':{'field':'taxonomies." + taxa.get("childRank") + ".tax_id.keyword', 'size': 20000}}}}}},");
            }
        }
        else if(isPhylogenyFilter) {
            sb.append("'childRank': { 'nested': { 'path':'taxonomies."+ phylogenyRank+"'},");
            sb.append("'aggs':{'scientificName':{'terms':{'field':'taxonomies."+phylogenyRank+".scientificName', 'size': 20000},");
            sb.append("'aggs':{'commonName':{'terms':{'field':'taxonomies."+phylogenyRank+".commonName', 'size': 20000}},");
            sb.append("'taxId':{'terms':{'field':'taxonomies." + phylogenyRank + ".tax_id.keyword', 'size': 20000}}}}}},");
        }

        sb.append("'biosamples': {'terms': {'field': 'biosamples.keyword'}},");
        sb.append("'raw_data': {'terms': {'field': 'raw_data.keyword'}},");
        sb.append("'mapped_reads': {'terms': {'field': 'mapped_reads.keyword'}},");
        sb.append("'assemblies': {'terms': {'field': 'assemblies.keyword'}},");
        sb.append("'annotation_complete': {'terms': {'field': 'annotation_complete.keyword'}},");
        sb.append("'annotation': {'terms': {'field': 'annotation.keyword'}}");
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

        sb.append("'query' : ");
        sb.append("{'multi_match': {");
        sb.append("'operator': 'AND',");
        sb.append("'query' : '" + searchQuery.toString() + "',");
        sb.append("'fields' : ['organism.autocomp', 'commonName.autocomp', 'biosamples.autocomp','raw_data.autocomp'," +
                "'mapped_reads.autocomp'," +
                "'assemblies.autocomp','annotation_complete.autocomp','annotation.autocomp', " +
                "'symbionts_records.organism.text.autocomp']");
        sb.append("}},");

        sb.append("'aggregations': {");
        sb.append("'symbionts_biosamples_status': {'terms': {'field': 'symbionts_biosamples_status'}},");
        sb.append("'symbionts_raw_data_status': {'terms': {'field': 'symbionts_raw_data_status'}},");
        sb.append("'symbionts_assemblies_status': {'terms': {'field': 'symbionts_assemblies_status'}},");
        sb.append("'biosamples': {'terms': {'field': 'biosamples.keyword'}},");
        sb.append("'raw_data': {'terms': {'field': 'raw_data.keyword'}},");
        sb.append("'mapped_reads': {'terms': {'field': 'mapped_reads.keyword'}},");
        sb.append("'assemblies': {'terms': {'field': 'assemblies.keyword'}},");
        sb.append("'annotation_complete': {'terms': {'field': 'annotation_complete.keyword'}},");
        sb.append("'annotation': {'terms': {'field': 'annotation.keyword'}},");
        sb.append("'kingdomRank': { 'nested': { 'path':'taxonomies.kingdom'},");
        sb.append("'aggs':{'scientificName':{'terms':{'field':'taxonomies.kingdom.scientificName', 'size': 20000},");
        sb.append("'aggs':{'commonName':{'terms':{'field':'taxonomies.kingdom.commonName', 'size': 20000}},");
        sb.append("'taxId':{'terms':{'field':'taxonomies.kingdom.tax_id.keyword', 'size': 20000}}}}}}");
        sb.append("}");

        sb.append("}");
        String query = sb.toString().replaceAll("'", "\"");
        return query;
    }
    private static final String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
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
            httpPost.setHeader("Authorization", getBasicAuthenticationHeader(esUsername, esPassword));

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

    private String getRequest(String baseURL) {
        CloseableHttpClient client = HttpClients.createDefault();
        StringEntity entity = null;
        String resp = "";
        try {
            HttpGet httpGET = new HttpGet(baseURL);
            httpGET.setHeader("Accept", "application/json");
            httpGET.setHeader("Content-type", "application/json");
//            httpGET.setHeader("Authorization", getBasicAuthenticationHeader(esUsername, esPassword));
            CloseableHttpResponse rs = client.execute(httpGET);
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
    public ByteArrayInputStream csvDownload(Optional<String> search, Optional<String> filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> taxonomyFilter) throws ParseException, IOException {
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        String query = this.filterQueryGenerator(search, filter, from.get(), size.get(), sortColumn, sortOrder, taxonomyFilter);

        respString = this.postRequest( esConnectionURL + "/tracking_status_index/_search", query);
        JSONParser parser = new JSONParser();
        jsonResponse = (JSONObject) parser.parse(respString);
        JSONArray jsonList =  (JSONArray) ((JSONObject) jsonResponse.get("hits")).get("hits");
        ByteArrayInputStream csv = createCsv(jsonList);
        return csv;
    }

    private ByteArrayInputStream createCsv(JSONArray jsonList) throws IOException {
        String[] header = {"Organism", "Common Name", "Metadata submitted to BioSamples", "Raw data submitted to ENA", "Mapped reads submitted to ENA", "Assemblies submitted to ENA", "Annotation complete", "Annotation submitted to ENA"};
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT.withHeader(header));) {
            for (int i = 0; i < jsonList.size(); i++) {
                JSONObject obj = (JSONObject) ((JSONObject) jsonList.get(i)).get("_source");
                String organism = "";
                String commonName = "-";
                String biosamples = "";
                String rawData = "";
                String mappedReads = "";
                String assemblies = "";
                String annotationComplete = "";
                String annotation = "";

                organism = obj.get("organism").toString();

                if(obj.get("commonName") != null) {
                    commonName = obj.get("commonName").toString();
                }
                biosamples = obj.get("biosamples").toString();
                rawData = obj.get("raw_data").toString();
                mappedReads = obj.get("mapped_reads").toString();
                assemblies = obj.get("assemblies").toString();
                annotationComplete = obj.get("annotation_complete").toString();
                annotation = obj.get("annotation").toString();

                List<String> record = Arrays.asList(
                        organism, commonName, biosamples, rawData, mappedReads, assemblies, annotationComplete, annotation);
                csvPrinter.printRecord(record);
            }
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to CSV file: " + e.getMessage());
        }
    }
}
