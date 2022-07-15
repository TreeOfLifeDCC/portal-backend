package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.RootOrganism;
import com.dtol.platform.es.mapping.SecondaryOrganism;
import com.dtol.platform.es.repository.RootOrganismRepository;
import com.dtol.platform.es.service.RootSampleService;
import io.netty.util.internal.StringUtil;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Transactional
public class RootSampleServiceImpl implements RootSampleService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    RootOrganismRepository rootOrganismRepository;
    @Value("${ES_CONNECTION_URL}")
    String esConnectionURL;
    static final String[] taxaRankArray = {"superkingdom", "kingdom", "subkingdom", "superphylum", "phylum", "subphylum", "superclass", "class", "subclass", "infraclass", "cohort", "subcohort", "superorder", "order", "suborder", "infraorder", "parvorder", "section", "subsection", "superfamily", "family", "subfamily", "tribe", "subtribe", "genus", "series", "subgenus", "species_group", "species_subgroup", "species", "subspecies", "varietas", "forma"};

    @Override
    public JSONArray findAllOrganisms(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder) throws ParseException {
        StringBuilder sb = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);

        sb.append("{");
        sb.append("'from' :" + page + ",'size':" + size + ",");
        if (sort.length() != 0)
            sb.append(sort);
        sb.append("'query' : { 'match_all' : {}}");
        sb.append("}");

        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/data_portal/_search", query);
        JSONArray respArray = (JSONArray) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("hits")).get("hits");
        return respArray;
    }


    @Override
    public Map<String, List<JSONObject>> getRootOrganismFilters() throws ParseException {
        Map<String, List<JSONObject>> filterMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append("{'size':0, 'aggregations':{");
        sb.append("'trackingSystem': { 'nested': { 'path':'trackingSystem'},");
        sb.append("'aggs':{");
        sb.append("'rank':{'terms':{'field':'trackingSystem.rank', 'order': { '_key' : 'desc' }},");
        sb.append("'aggregations':{ 'name': {'terms':{'field':'trackingSystem.name'},");
        sb.append("'aggregations':{ 'status': {'terms':{'field':'trackingSystem.status'}");
        sb.append("}}}}}}},");

        sb.append("'genome': { 'nested': { 'path':'genome_notes'},");
        sb.append("'aggs':{");
        sb.append("'genome_count':{'cardinality':{'field':'genome_notes.id'}");
        sb.append("}}}}");

        sb.append("}");

        String query = sb.toString().replaceAll("'", "\"");

        String respString = this.postRequest("http://" + esConnectionURL + "/data_portal/_search", query);
        JSONObject aggregations = (JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations")).get("trackingSystem")).get("rank");
        JSONArray trackFilterArray = (JSONArray) (aggregations.get("buckets"));
        for (int i = 0; i < trackFilterArray.size(); i++) {
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

        String genomeCount = (String) ((JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations")).get("genome")).get("doc_count").toString();
        JSONObject obj = new JSONObject();
        List<JSONObject> arr = new ArrayList<JSONObject>();

        obj.put("key", "Genome Notes - Submitted");
        obj.put("doc_count", Integer.valueOf(genomeCount));
        arr.add(obj);
        filterMap.put("genome", arr);

        return filterMap;
    }


    public Map<String, List<JSONObject>> getExperimentTypeFilters() throws ParseException {
        Map<String, List<JSONObject>> filterMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append("{'size':0, 'aggregations':{");
        sb.append("'experiment': { 'nested': { 'path':'experiment'},");
        sb.append("'aggs':{");
        sb.append("'library_construction_protocol':{'terms':{'field':'experiment.library_construction_protocol.keyword'},");
        sb.append("'aggs' : { 'organism_count' : { 'reverse_nested' : {}}");
        sb.append("}}}}}}");



        String query = sb.toString().replaceAll("'", "\"");

        String respString = this.postRequest("http://" + esConnectionURL + "/data_portal/_search", query);
        JSONObject aggregations = (JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations")).get("experiment")).get("library_construction_protocol");
        JSONArray libraryConstructionProtocol = (JSONArray) (aggregations.get("buckets"));

        List<JSONObject> libraryConstructionArray = new ArrayList<JSONObject>();
        for (int j = 0; j < libraryConstructionProtocol.size(); j++) {
            JSONObject temp = (JSONObject) libraryConstructionProtocol.get(j);
            JSONObject filterObj = new JSONObject();
            filterObj.put("key",   temp.get("key"));
            filterObj.put("organism_count", ((JSONObject ) temp.get("organism_count")));
            libraryConstructionArray.add(filterObj);

        }
        filterMap.put("Experiment_type",libraryConstructionArray);
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
        String respString = this.postRequest("http://" + esConnectionURL + "/data_portal/_search", query);
        JSONObject aggregations = (JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations")).get("filters");
        JSONArray sexFilter = (JSONArray) ((JSONObject) aggregations.get("sex_filter")).get("buckets");
        JSONArray trackFilter = (JSONArray) ((JSONObject) aggregations.get("tracking_status_filter")).get("buckets");
        JSONArray orgPartFilterObj = (JSONArray) ((JSONObject) aggregations.get("organism_part_filter")).get("buckets");

        filterMap.put("sex", sexFilter);
        filterMap.put("trackingSystem", trackFilter);
        filterMap.put("organismPart", orgPartFilterObj);

        return filterMap;

    }

    @Override
    public String findSecondaryOrganismFilterResults(String organism, String filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getSecondaryOrganismFilterResultQuery(organism, filter, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://" + esConnectionURL + "/data_portal/_search", query);
        return respString;
    }

    @Override
    public String findRootOrganismFilterResults(Optional<String> search, Optional<String> filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> taxonomyFilter) throws ParseException {
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getOrganismFilterQuery(search, filter, from.get(), size.get(), sortColumn, sortOrder, taxonomyFilter);
        respString = this.postRequest("http://" + esConnectionURL + "/data_portal/_search", query);
        return respString;
    }

    @Override
    public String findRootOrganismSearchResult(String search, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder) {
        List<SecondaryOrganism> results = new ArrayList<SecondaryOrganism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getRootOrganismSearchQuery(search, from.get(), size.get(), sortColumn, sortOrder);
        respString = this.postRequest("http://" + esConnectionURL + "/data_portal/_search", query);

        return respString;
    }

    private StringBuilder getSortQuery(Optional<String> sortColumn, Optional<String> sortOrder) {
        StringBuilder sort = new StringBuilder();
        sort.append("'sort' : [");
        String sortColumnName = "";
        if (sortColumn.isPresent()) {
            sortColumnName = sortColumn.get();
            if (sortColumnName.equals("annotation")) {
                sortColumnName = "annotation_status";
            } else if (sortColumnName.equals("assemblies")) {
                sortColumnName = "assemblies_status";
            }

            if (sortOrder.get().equals("asc")) {
                sort.append("{'" + sortColumnName + "':'asc'}");
            } else {
                sort.append("{'" + sortColumnName + "':'desc'}");
            }
        } else {
            sort.append("{'trackingSystem.rank':{'order':'desc','nested_path':'trackingSystem', 'nested_filter':{'term':{'trackingSystem.status':'Done'}}}}");
        }
        sort.append("],");

        return sort;
    }

    private String getSecondaryOrganismFilterResultQuery(String organism, String filter, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder) {
        String[] filterArray = filter.split(",");
        StringBuilder sb = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);

        sb.append("{");
        sb.append("'query' : { 'bool' : { 'should' : [");

        sb.append("{'terms' : {'organism':[");
        sb.append("'" + organism + "'");
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

    private String getOrganismFilterQuery(Optional<String> search, Optional<String> filter, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> taxonomyFilter) throws ParseException {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbt = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);
        Boolean isPhylogenyFilter = false;
        String phylogenyRank = "";
        String phylogenyTaxId = "";
        StringBuilder searchQuery = new StringBuilder();

        if (search.isPresent()) {
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
            sb.append("{'query_string': {");
            sb.append("'query' : '" + searchQuery.toString() + "',");
            sb.append("'fields' : ['organism.normalize','commonName.normalize', 'biosamples','raw_data','mapped_reads','assemblies_status','annotation_complete','annotation_status']");
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
                        sbt.append("{ 'nested' : { 'path': 'taxonomies." + taxa.get("rank") + "', 'query' : ");
                        sbt.append("{ 'bool' : { 'must' : [");
                        sbt.append("{ 'term' : { 'taxonomies.");
                        sbt.append(taxa.get("rank") + ".scientificName': '" + taxa.get("taxonomy") + "'}}");
                        sbt.append("]}}}}}}");
                    } else {
                        if (i == taxaTree.size() - 1) {
                            sbt.append("{ 'nested' : { 'path': 'taxonomies', 'query' : ");
                            sbt.append("{ 'nested' : { 'path': 'taxonomies." + taxa.get("rank") + "', 'query' : ");
                            sbt.append("{ 'bool' : { 'must' : [");
                            sbt.append("{ 'term' : { 'taxonomies.");
                            sbt.append(taxa.get("rank") + ".scientificName': '" + taxa.get("taxonomy") + "'}}");
                            sbt.append("]}}}}}}");
                        } else {
                            sbt.append("{ 'nested' : { 'path': 'taxonomies', 'query' : ");
                            sbt.append("{ 'nested' : { 'path': 'taxonomies." + taxa.get("rank") + "', 'query' : ");
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
                    sb.append("{'terms' : {'assemblies_status':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Annotation complete")) {
                    sb.append("{'terms' : {'annotation_complete':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Annotation")) {
                    sb.append("{'terms' : {'annotation_status':[");
                    sb.append("'" + splitArray[1].trim() + "'");
                    sb.append("]}},");
                } else if (splitArray[0].trim().equals("Genome Notes")) {
                    sb.append("{ 'nested': {'path': 'genome_notes','query': {'bool': {'must': [{'exists': {'field': 'genome_notes.url'}}]}}}},");
                } else if (Arrays.asList(taxaRankArray).contains(splitArray[0].trim())) {
                    isPhylogenyFilter = true;
                    phylogenyRank = splitArray[0].trim();
                    phylogenyTaxId = splitArray[1].trim();
                    sb.append("{ 'nested' : { 'path': 'taxonomies', 'query' : ");
                    sb.append("{ 'nested' : { 'path': 'taxonomies." + splitArray[0].trim() + "', 'query' : ");
                    sb.append("{ 'bool' : { 'must' : [");
                    sb.append("{ 'term' : { 'taxonomies.");
                    sb.append(splitArray[0].trim() + ".tax_id': '" + splitArray[1].trim() + "'}}");
                    sb.append("]}}}}}},");
                }else {
                    sb.append("{ 'nested' : { 'path': 'experiment', 'query' : ");
                    sb.append("{ 'bool' : { 'must' : [");
                    sb.append("{ 'term' : { 'experiment.library_construction_protocol.keyword' : '"+ filterArray[i]+ "'"  );
                    sb.append("}}]}}}},");
                }
            }
            sb.append("]}},");
        } else {
            sb.append(sbt.toString());
            sb.append("]}},");
        }

        sb.append("'aggregations': {");
        sb.append("'kingdomRank': { 'nested': { 'path':'taxonomies.kingdom'},");
        sb.append("'aggs':{'scientificName':{'terms':{'field':'taxonomies.kingdom.scientificName', 'size': 20000},");
        sb.append("'aggs':{'commonName':{'terms':{'field':'taxonomies.kingdom.commonName', 'size': 20000}},");
        sb.append("'taxId':{'terms':{'field':'taxonomies.kingdom.tax_id.keyword', 'size': 20000}}}}}},");
        if (taxonomyFilter.isPresent() && !taxonomyFilter.get().equals("undefined") && !isPhylogenyFilter) {
            JSONArray taxaTree = (JSONArray) new JSONParser().parse(taxonomyFilter.get().toString());
            if (taxaTree.size() > 0) {
                JSONObject taxa = (JSONObject) taxaTree.get(taxaTree.size() - 1);
                sb.append("'childRank': { 'nested': { 'path':'taxonomies." + taxa.get("childRank") + "'},");
                sb.append("'aggs':{'scientificName':{'terms':{'field':'taxonomies." + taxa.get("childRank") + ".scientificName', 'size': 20000},");
                sb.append("'aggs':{'commonName':{'terms':{'field':'taxonomies." + taxa.get("childRank") + ".commonName', 'size': 20000}},");
                sb.append("'taxId':{'terms':{'field':'taxonomies." + taxa.get("childRank") + ".tax_id.keyword', 'size': 20000}}}}}},");
            }
        } else if (isPhylogenyFilter) {
            sb.append("'childRank': { 'nested': { 'path':'taxonomies." + phylogenyRank + "'},");
            sb.append("'aggs':{'scientificName':{'terms':{'field':'taxonomies." + phylogenyRank + ".scientificName', 'size': 20000},");
            sb.append("'aggs':{'commonName':{'terms':{'field':'taxonomies." + phylogenyRank + ".commonName', 'size': 20000}},");
            sb.append("'taxId':{'terms':{'field':'taxonomies." + phylogenyRank + ".tax_id.keyword', 'size': 20000}}}}}},");
        }

        sb.append("'biosamples': {'terms': {'field': 'biosamples'}},");
        sb.append("'raw_data': {'terms': {'field': 'raw_data'}},");
        sb.append("'mapped_reads': {'terms': {'field': 'mapped_reads'}},");
        sb.append("'assemblies': {'terms': {'field': 'assemblies_status'}},");
        sb.append("'annotation_complete': {'terms': {'field': 'annotation_complete'}},");
        sb.append("'annotation': {'terms': {'field': 'annotation_status'}},");
        sb.append("'experiment': { 'nested': { 'path':'experiment'},");
        sb.append("'aggs':{");
        sb.append("'library_construction_protocol':{'terms':{'field':'experiment.library_construction_protocol.keyword'},");
        sb.append("'aggs' : { 'organism_count' : { 'reverse_nested' : {}}");
        sb.append("}}}},");
        sb.append("'genome': { 'nested': { 'path':'genome_notes'},");
        sb.append("'aggs':{");
        sb.append("'genome_count':{'cardinality':{'field':'genome_notes.id'}");
        sb.append("}}}");

        sb.append("}");

        sb.append("}");

        String query = sb.toString().replaceAll("'", "\"").replaceAll(",]", "]");

        return query;
    }

    private String getRootOrganismSearchQuery(String search, String from, String size, Optional<String> sortColumn, Optional<String> sortOrder) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sort = this.getSortQuery(sortColumn, sortOrder);
        StringBuilder searchQuery = new StringBuilder();
        String[] searchArray = search.split(" ");
        for (String temp : searchArray) {
            searchQuery.append("*" + temp + "*");
        }
        sb.append("{");
        if (from.equals("undefined") && size.equals("undefined")) {
            sb.append("'from' :" + 0 + ",'size':" + 15 + ",");
        } else {
            sb.append("'from' :" + from + ",'size':" + size + ",");
        }
        if (sort.length() != 0)
            sb.append(sort);
        sb.append("'query': {");
        sb.append("'query_string': {");
        sb.append("'query' : '" + searchQuery.toString() + "',");
        sb.append("'fields' : ['organism.normalize','commonName.normalize', 'biosamples','raw_data','mapped_reads','assemblies_status','annotation_complete','annotation_status']");
        sb.append("}},");

        sb.append("'aggregations': {");
        sb.append("'biosamples': {'terms': {'field': 'biosamples'}},");
        sb.append("'raw_data': {'terms': {'field': 'raw_data'}},");
        sb.append("'mapped_reads': {'terms': {'field': 'mapped_reads'}},");
        sb.append("'assemblies': {'terms': {'field': 'assemblies_status'}},");
        sb.append("'annotation_complete': {'terms': {'field': 'annotation_complete'}},");
        sb.append("'annotation': {'terms': {'field': 'annotation_status'}},");
        sb.append("'kingdomRank': { 'nested': { 'path':'taxonomies.kingdom'},");
        sb.append("'aggs':{'scientificName':{'terms':{'field':'taxonomies.kingdom.scientificName', 'size': 20000},");
        sb.append("'aggs':{'commonName':{'terms':{'field':'taxonomies.kingdom.commonName', 'size': 20000}},");
        sb.append("'taxId':{'terms':{'field':'taxonomies.kingdom.tax_id.keyword', 'size': 20000}}}}}},");
        sb.append("'experiment': { 'nested': { 'path':'experiment'},");
        sb.append("'aggs':{");
        sb.append("'library_construction_protocol':{'terms':{'field':'experiment.library_construction_protocol.keyword'},");
        sb.append("'aggs' : { 'organism_count' : { 'reverse_nested' : {}}");
        sb.append("}}}},");
        sb.append("'genome': { 'nested': { 'path':'genome_notes'},");
        sb.append("'aggs':{");
        sb.append("'genome_count':{'cardinality':{'field':'genome_notes.id'}");
        sb.append("}}}");

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
    public long getRootOrganismCount() throws ParseException {
        String respString = this.getRequest("http://" + esConnectionURL + "/data_portal/_count");
        JSONObject resp = (JSONObject) new JSONParser().parse(respString);
        long count = Long.valueOf(resp.get("count").toString());
        return count;
    }

    @Override
    public long getRelatedOrganismCount() throws ParseException {
        String respString = this.getRequest("http://" + esConnectionURL + "/data_portal/_count");
        JSONObject resp = (JSONObject) new JSONParser().parse(respString);
        long count = Long.valueOf(resp.get("count").toString());
        return count;
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
        sb.append("'missing_bucket': true,'order':" + ((sortOrder.isPresent() && sortColumn.get().toString().equals("organism")) ? "'" + sortOrder.get().toString() + "'}}}," : "'asc'}}},"));

        sb.append("{'commonName' : {'terms': {'field': 'commonName',");
        sb.append("'missing_bucket': true,'order':" + ((sortOrder.isPresent() && sortColumn.get().toString().equals("commonName")) ? "'" + sortOrder.get().toString() + "'}}}," : "'asc'}}},"));

        sb.append("{'sex' : {'terms': {'field': 'sex',");
        sb.append("'missing_bucket': true,'order':" + ((sortOrder.isPresent() && sortColumn.get().toString().equals("sex")) ? "'" + sortOrder.get().toString() + "'}}}," : "'asc'}}},"));

        sb.append("{'trackingSystem' : {'terms': {'field': 'trackingSystem',");
        sb.append("'missing_bucket': true,'order':" + ((sortOrder.isPresent() && sortColumn.get().toString().equals("trackingSystem")) ? "'" + sortOrder.get().toString() + "'}}}" : "'asc'}}}"));

        if (afterKey.isPresent())
            sb.append(",'after':" + afterKey.get().toString());
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
        String query = this.getDistinctRootSamplesByOrganismQuery(size, sortColumn, sortOrder, afterKey);
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

    @Override
    public JSONObject findRootSampleById(String id) throws ParseException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'query' : { 'bool' : { 'must' : [");
        sb.append("{'terms' : {'_id':['");
        sb.append(id);
        sb.append("']}}]}}}");
        String query = sb.toString().replaceAll("'", "\"");

        String respString = this.postRequest("http://" + esConnectionURL + "/data_portal/_search", query);
        JSONObject resp = (JSONObject) ((JSONArray) ((JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("hits"))).get("hits")).get(0);
        JSONObject source = (JSONObject) resp.get("_source");
        JSONArray experiment = (JSONArray) source.get("experiment");
        JSONArray expArray = new JSONArray();
        if (experiment != null) {
            for (int i = 0; i < experiment.size(); i++) {
                JSONObject obj = (JSONObject) experiment.get(i);
                String fast_q = (String) obj.get("fastq_ftp");
                String[] fast_q_array = fast_q.split(";");

                String submitted_ftp = (String) obj.get("submitted_ftp");
                String[] submitted_ftp_array = submitted_ftp.split(";");

                String sra_ftp = (String) obj.get("sra_ftp");
                String[] sra_ftp_array = sra_ftp.split(";");

                obj.put("fastq_ftp", fast_q_array);
                obj.put("submitted_ftp", submitted_ftp_array);
                obj.put("sra_ftp", sra_ftp_array);
                expArray.add(obj);
            }
            source.put("experiment", expArray);
        }

        return source;
    }

    @Override
    public ByteArrayInputStream csvDownload(Optional<String> search, Optional<String> filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> taxonomyFilter) throws ParseException, IOException {
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        String query = this.getOrganismFilterQuery(search, filter, from.get(), size.get(), sortColumn, sortOrder, taxonomyFilter);
        respString = this.postRequest("http://" + esConnectionURL + "/data_portal/_search", query);
        JSONParser parser = new JSONParser();
        jsonResponse = (JSONObject) parser.parse(respString);
        JSONArray jsonList = (JSONArray) ((JSONObject) jsonResponse.get("hits")).get("hits");
        ByteArrayInputStream csv = createCsv(jsonList);
        return csv;
    }

    private ByteArrayInputStream createCsv(JSONArray jsonList) throws IOException {
        String[] header = {"Organism", "ToL ID", "INSDC ID", "Common Name", "Current status", "External references"};
        JSONObject tolCodes = getTolCodes();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT.withHeader(header));) {
            for (int i = 0; i < jsonList.size(); i++) {
                JSONObject obj = (JSONObject) ((JSONObject) jsonList.get(i)).get("_source");
                String organism = "";
                String insdc = "-";
                String commonName = "-";
                String goatInfo = "";
                String genome = "";
                String tolqc = "";
                String tolid = "-";

                organism = obj.get("organism").toString();

                if (obj.get("experiment") != null && (((JSONArray) obj.get("experiment")).size() > 0)) {
                    insdc = ((JSONObject) (((JSONArray) obj.get("experiment")).get(0))).get("study_accession").toString();
                }
                if (obj.get("commonName") != null) {
                    commonName = obj.get("commonName").toString();
                }
                if (obj.get("genome_notes") != null && (((JSONArray) obj.get("genome_notes")).size() > 0)) {
                    genome = ((JSONObject) (((JSONArray) obj.get("genome_notes")).get(0))).get("url").toString();
                }
                if (obj.get("goat_info") != null && ((JSONObject) obj.get("goat_info")).size() > 0) {
                    goatInfo = ((JSONObject) obj.get("goat_info")).get("url").toString();
                }
                if (obj.get("tolid") != null) {
                    tolid = obj.get("tolid").toString();
                    String organismName = organism.replaceAll(" ", "-");
                    String clade = tolCodes.get(Character.toString(tolid.charAt(0))).toString();
                    tolqc = "https://tolqc.cog.sanger.ac.uk/darwin/" + clade + "/" + organismName;
                }
                String externalRef = (!goatInfo.isEmpty() ? goatInfo + ";" : "") + (!tolqc.isEmpty() ? tolqc + ";" : "") + (!genome.isEmpty() ? genome : "");

                List<String> record = Arrays.asList(
                        organism, tolid, insdc, commonName, obj.get("currentStatus").toString(), externalRef);
                csvPrinter.printRecord(record);
            }
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to CSV file: " + e.getMessage());
        }
    }

    JSONObject getTolCodes() {
        JSONObject obj = new JSONObject();
        obj.put("m", "mammals");
        obj.put("d", "dicots");
        obj.put("i", "insects");
        obj.put("u", "algae");
        obj.put("p", "protists");
        obj.put("x", "molluscs");
        obj.put("t", "other-animal-phyla");
        obj.put("q", "arthropods");
        obj.put("k", "chordates");
        obj.put("f", "fish");
        obj.put("a", "amphibians");
        obj.put("b", "birds");
        obj.put("e", "echinoderms");
        obj.put("w", "annelids");
        obj.put("j", "jellyfish");
        obj.put("h", "platyhelminths");
        obj.put("n", "nematodes");
        obj.put("v", "vascular-plants");
        obj.put("l", "monocots");
        obj.put("c", "non-vascular-plants");
        obj.put("g", "fungi");
        obj.put("o", "sponges");
        obj.put("r", "reptiles");
        obj.put("s", "sharks");
        obj.put("y", "bacteria");
        obj.put("z", "archea");
        return obj;
    }

    private String getRequest(String baseURL) {
        CloseableHttpClient client = HttpClients.createDefault();
        StringEntity entity = null;
        String resp = "";
        try {
            HttpGet httpGET = new HttpGet(baseURL);
            httpGET.setHeader("Accept", "application/json");
            httpGET.setHeader("Content-type", "application/json");
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
    public ByteArrayInputStream getDataFiles(Optional<String> search, Optional<String> filter, Optional<String> from, Optional<String> size, Optional<String> sortColumn, Optional<String> sortOrder, Optional<String> taxonomyFilter, String downloadOption) throws ParseException, IOException {
        JSONObject jsonResponse = new JSONObject();
        StringBuilder sb = new StringBuilder();
        String query = this.getOrganismFilterQuery(search, filter, from.get(), size.get(), sortColumn, sortOrder, taxonomyFilter);
        String respString = this.postRequest("http://" + esConnectionURL + "/data_portal/_search", query);
        ByteArrayInputStream csv = null;
        JSONParser parser = new JSONParser();
        jsonResponse = (JSONObject) parser.parse(respString);
        JSONArray jsonList = (JSONArray) ((JSONObject) jsonResponse.get("hits")).get("hits");
        csv = createDataFilesCSV(jsonList, downloadOption);
        return csv;
    }

    @Override
    public JSONArray findGisSearchResult(String search) throws ParseException {
        List<SecondaryOrganism> results = new ArrayList<SecondaryOrganism>();
        String respString = null;
        JSONObject jsonResponse = new JSONObject();
        HashMap<String, Object> response = new HashMap<>();
        String query = this.getGisSearchQuery(search);
        respString = this.postRequest("http://" + esConnectionURL + "/gis/_search", query);

        JSONArray respArray = (JSONArray) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("hits")).get("hits");
        return respArray;

    }

    private ByteArrayInputStream createDataFilesCSV(JSONArray jsonList, String downloadOption) throws IOException {
        String[] header = {};
        if (downloadOption.equalsIgnoreCase("assemblies")) {
            header = new String[]{"Scientific Name", "Accession", "Version", "Assembly Name", "Assembly Description", "Link to chromosomes, contigs and scaffolds all in one"};
        } else if (downloadOption.equalsIgnoreCase("annotation")) {
            header = new String[]{"Annotation GTF", "Annotation GFF3", "Proteins Fasta", "Transcripts Fasta", "Softmasked genomes Fasta"};
        } else if (downloadOption.equalsIgnoreCase("raw_files")) {
            header = new String[]{"Study Accession","Sample Accession","Experiment Accession","Run Accession","Tax Id","Scientific Name","FASTQ FTP","Submitted FTP","SRA FTP","Library Construction Protocol"};
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT.withHeader(header));) {
            for (Object item : jsonList) {
                JSONObject obj = ((JSONObject) ((JSONObject) item).get("_source"));
                if (downloadOption.equalsIgnoreCase("assemblies")) {
                    JSONArray list = ((JSONArray) obj.get("assemblies"));
                    String scientificName = (String) obj.get("organism");
                    if (list != null) {
                        for (Object o : list) {
                            JSONObject objass = (JSONObject) o;
                            String accession = "-";
                            String version = "-";
                            String assemblyName = "";
                            String assemblyDescription = "";
                            String link = "";
                            if (objass.get("assembly_name") != null) {
                                assemblyName = objass.get("assembly_name").toString();
                            }

                            if (objass.get("version") != null) {
                                version = objass.get("version").toString();
                            }

                            if (objass.get("accession") != null) {
                                accession = objass.get("accession").toString();
                            }
                            if (objass.get("description") != null) {
                                assemblyDescription = objass.get("description").toString();
                            }
                            if (!StringUtil.isNullOrEmpty(accession)) {
                                link = "https://www.ebi.ac.uk/ena/browser/api/fasta/" + accession + "?download=true&gzip=true";
                            }
//
                            List<String> record = Arrays.asList(
                                    scientificName, accession, version, assemblyName, assemblyDescription, link);
                            csvPrinter.printRecord(record);
                        }
                    }
                } else if (downloadOption.equalsIgnoreCase("annotation")) {
                    JSONArray annotationList = ((JSONArray) obj.get("annotation"));
                    if (annotationList != null) {
                        for (int i = 0; i < annotationList.size(); i++) {
                            JSONObject annotationoObj = (JSONObject) annotationList.get(i);
                            String gtf = "-";
                            String gff3 = "-";
                            String proteinsFasta = "";
                            String transcriptsFasta = "";
                            String softmaskedGenomesFasta = "";
                            if (annotationoObj.get("annotation") != null) {
                                JSONObject obj2 = ((JSONObject) annotationoObj.get("annotation"));
                                gtf = obj2.get("GTF").toString();
                                gff3 = obj2.get("GFF3").toString();
                                if (annotationoObj.get("proteins") != null) {
                                    proteinsFasta = ((JSONObject) annotationoObj.get("proteins")).get("FASTA").toString();

                                }
                                if (annotationoObj.get("transcripts") != null) {
                                    transcriptsFasta = ((JSONObject) annotationoObj.get("transcripts")).get("FASTA").toString();

                                }

                                if (annotationoObj.get("softmasked_genome") != null) {
                                    softmaskedGenomesFasta = ((JSONObject) annotationoObj.get("softmasked_genome")).get("FASTA").toString();

                                }
                            }
                            List<String> record = Arrays.asList(gtf,
                                    gff3, proteinsFasta, transcriptsFasta, softmaskedGenomesFasta);
                            csvPrinter.printRecord(record);

                        }

                    }
                } else if (downloadOption.equalsIgnoreCase("raw_files")) {
                    JSONArray list = ((JSONArray) obj.get("experiment"));
                    if (list != null) {
                        for (Object value : list) {
                            JSONObject experimentObj = (JSONObject) value;
                            String fASTQ_FTP = "";
                            String studyAccession="";
                            String sampleAccession="";
                            String experimentAccession="";
                            String runAccession="";
                            String taxId="";
                            String scientificName="";
                            String submittedFTP="";
                            String sRAFTP="";
                            String libraryConstructionProtocol="";
                            if (experimentObj.get("study_accession") != null) {
                                studyAccession = experimentObj.get("study_accession").toString();
                            }
                            if (experimentObj.get("sample_accession") != null) {
                                sampleAccession = experimentObj.get("sample_accession").toString();
                            }
                            if (experimentObj.get("experiment_accession") != null) {
                                experimentAccession = experimentObj.get("experiment_accession").toString();
                            }
                            if (experimentObj.get("run_accession") != null) {
                                runAccession = experimentObj.get("run_accession").toString();
                            }
                            if (experimentObj.get("tax_id") != null) {
                                taxId = experimentObj.get("tax_id").toString();
                            }
                            if (experimentObj.get("scientific_name") != null) {
                                scientificName = experimentObj.get("scientific_name").toString();
                            }
                            if (experimentObj.get("submitted_ftp") != null) {
                                submittedFTP = experimentObj.get("submitted_ftp").toString();
                            }
                            if (experimentObj.get("sra-ftp") != null) {
                                sRAFTP = experimentObj.get("sra-ftp").toString();
                            }
                            if (experimentObj.get("library_construction_protocol") != null) {
                                libraryConstructionProtocol = experimentObj.get("library_construction_protocol").toString();
                            }
                            if (experimentObj.get("fastq_ftp") != null) {
                                fASTQ_FTP = experimentObj.get("fastq_ftp").toString();
                                String[] fASTQ_FTPList = fASTQ_FTP.split(";");
                                if(fASTQ_FTPList.length> 1){
                                    for (String s : fASTQ_FTPList) {
                                        List<String> record = Arrays.asList(studyAccession,
                                                sampleAccession,
                                                experimentAccession,
                                                runAccession,
                                                taxId,
                                                scientificName,
                                                s,
                                                submittedFTP,
                                                sRAFTP,
                                                libraryConstructionProtocol);
                                        csvPrinter.printRecord(record);
                                    }
                                }else{
                                    List<String> record = Arrays.asList(studyAccession,
                                            sampleAccession,
                                            experimentAccession,
                                            runAccession,
                                            taxId,
                                            scientificName,
                                            fASTQ_FTP,
                                            submittedFTP,
                                            sRAFTP,
                                            libraryConstructionProtocol);
                                    csvPrinter.printRecord(record);
                                }
                            }

                        }
                    }

                }
                csvPrinter.flush();
            }
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to CSV file: " + e.getMessage());
        }
    }

    @Override
    public JSONArray getGisData() throws ParseException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'from' :0 ,'size':1000000,");
        sb.append("'query' : { 'match_all' : {}}");
        sb.append("}");

        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/gis/_search", query);
        JSONArray respArray = (JSONArray) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("hits")).get("hits");
        return respArray;
    }

    private String getGisSearchQuery(String search) {
        StringBuilder sb = new StringBuilder();
        StringBuilder searchQuery = new StringBuilder();
        String[] searchArray = search.split(" ");
        for (String temp : searchArray) {
            searchQuery.append("*" + temp + "*");
        }
        sb.append("{");
        sb.append("'from' :" + 0 + ",'size':" + 100000 + ",");

        sb.append("'query': { 'bool': { 'should': [ ");

        sb.append("{'nested': {'path': 'organisms','query': {'bool': {'must': [{'query_string': {");
        sb.append("'query' : '" + searchQuery.toString() + "',");
        sb.append("'fields' : ['organisms.organism.normalize','organisms.commonName.normalize', 'organisms.accession.normalize','organisms.lat','organisms.lng']");
        sb.append("}}]}}}}");

        sb.append(",");

        sb.append("{'nested': {'path': 'specimens','query': {'bool': {'must': [{'query_string': {");
        sb.append("'query' : '" + searchQuery.toString() + "',");
        sb.append("'fields' : ['specimens.organism.normalize','specimens.commonName.normalize', 'specimens.accession.normalize','specimens.lat','specimens.lng']");
        sb.append("}}]}}}}");

        sb.append("]}}}");

        String query = sb.toString().replaceAll("'", "\"");
        return query;
    }

}
