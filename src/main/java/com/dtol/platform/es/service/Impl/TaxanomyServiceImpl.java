package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.service.TaxanomyService;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class TaxanomyServiceImpl implements TaxanomyService {

    @Value("${ES_CONNECTION_URL}")
    String esConnectionURL;

    @Override
    public String getAllTaxonomiesByType(String type) {
        Map<String, JSONArray> filterMap = new HashMap<String, JSONArray>();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'query' : { 'has_child' : { 'type' :'" + type + "',");
        sb.append("'query' : {'match_all':{}");
        sb.append("}}}}");

        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/ontology/_search", query);

        return respString;
    }

    @Override
    public String findTaxanomiesByParent(String parent) {
        Map<String, JSONArray> filterMap = new HashMap<String, JSONArray>();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("'query' : { 'parent_id' : { 'type' :'grandchild',");
        sb.append("'id' : '" + parent + "'");
        sb.append("}}}");

        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/ontology/_search", query);

        return respString;
    }

    @Override
    public Boolean findIfTaxanomyHasChild(String organism) {
        return null;
    }

    @Override
    public String getTaxonomicRanksAndCounts(Optional<String> taxonomy) throws ParseException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (taxonomy.isPresent() && !taxonomy.get().equals("")) {
            sb.append("'size':0,");
            sb.append("'query' : { 'nested' : { 'path': 'taxonomies', 'query' : ");
            sb.append("{ 'query_string' : { 'query' : '");
            sb.append(taxonomy.get() + "'");
            sb.append("}}}},");
        }

        getNestedOntologyAggregations(sb);

        sb.append("}}}}");
        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/data_portal_test/_search", query);
        JSONObject aggregations = (JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations")).get("filters");

        return aggregations.toJSONString();
    }

    @Override
    public String getChildTaxonomyRank(Optional<String> filter, String rank, String taxonomy, String childRank, String tree) throws ParseException {
        StringBuilder sb = new StringBuilder();
        JSONObject resp = new JSONObject();
        JSONArray taxaTree = (JSONArray) new JSONParser().parse(tree);
        sb.append("{");
        sb.append("'size':0,");
        sb.append("'query' : { 'bool' : { 'must' : [");
        sb.append("{ 'nested' : { 'path': 'taxonomies', 'query' : ");
        sb.append("{ 'bool' : { 'must' : [");

        for (int i = 0; i < taxaTree.size(); i++) {
            JSONObject taxa = (JSONObject) taxaTree.get(i);
            if (taxaTree.size() == 1) {
                sb.append("{ 'term' : { 'taxonomies.");
                sb.append(taxa.get("rank") + "': '" + taxa.get("taxonomy") + "'}}");
            } else {
                if (i == taxaTree.size() - 1) {
                    sb.append("{ 'term' : { 'taxonomies.");
                    sb.append(taxa.get("rank") + "': '" + taxa.get("taxonomy") + "'}}");
                } else {
                    sb.append("{ 'term' : { 'taxonomies.");
                    sb.append(taxa.get("rank") + "': '" + taxa.get("taxonomy") + "'}},");
                }
            }
        }
//        sb.append("{ 'term' : { 'taxonomies.");
//        sb.append(rank + "': '" + taxonomy + "'}}");
        sb.append("]}}}}");
        if (filter.isPresent()) {
            String[] filterArray = filter.get().split(",");
            if (filterArray.length > 0 && !filterArray[0].equals("")) {
                sb.append(",{'terms' : {'trackingSystem':[");
                for (int i = 0; i < filterArray.length; i++) {
                    if (i == 0)
                        sb.append("'" + filterArray[i] + "'");
                    else
                        sb.append(",'" + filterArray[i] + "'");
                }
                sb.append("]}}");
            }
        }

        sb.append("]}},");

        sb.append("'aggregations':{");
        sb.append("'filters': { 'nested': { 'path':'taxonomies'},");
        sb.append("'aggs':{");
        sb.append("'childRank':{'terms':{'field':'taxonomies." + childRank + "', 'size': 20000}}");
        sb.append("}}");
        sb.append("}}");
        String query = sb.toString().replaceAll("'", "\"");
        String respString = this.postRequest("http://" + esConnectionURL + "/data_portal_test/_search", query);
        JSONArray aggregations = (JSONArray) ((JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(respString)).get("aggregations")).get("filters")).get("childRank")).get("buckets");
        JSONObject childTaxa = new JSONObject();
        childTaxa.put("parent", taxonomy);
        childTaxa.put("rank", childRank);
        childTaxa.put("childData", aggregations);
        childTaxa.put("expanded", false);
        resp.put(rank, childTaxa);
        return resp.toJSONString();
    }

    private void getNestedOntologyAggregations(StringBuilder sb) {
        sb.append("'aggregations':{");
        sb.append("'filters': { 'nested': { 'path':'taxonomies'},");
        sb.append("'aggs':{");
        sb.append("'class':{'terms':{'field':'taxonomies.class', 'size': 20000}},");
        sb.append("'cohort':{'terms':{'field':'taxonomies.cohort', 'size': 20000}},");
        sb.append("'family':{'terms':{'field':'taxonomies.family', 'size': 20000}},");
        sb.append("'forma':{'terms':{'field':'taxonomies.forma', 'size': 20000}},");
        sb.append("'genus':{'terms':{'field':'taxonomies.genus', 'size': 20000}},");
        sb.append("'infraclass':{'terms':{'field':'taxonomies.infraclass', 'size': 20000}},");
        sb.append("'infraorder':{'terms':{'field':'taxonomies.infraorder', 'size': 20000}},");
        sb.append("'kingdom':{'terms':{'field':'taxonomies.kingdom', 'size': 20000}},");
        sb.append("'order':{'terms':{'field':'taxonomies.order', 'size': 20000}},");
        sb.append("'parvorder':{'terms':{'field':'taxonomies.parvorder', 'size': 20000}},");
        sb.append("'phylum':{'terms':{'field':'taxonomies.phylum', 'size': 20000}},");
        sb.append("'section':{'terms':{'field':'taxonomies.section', 'size': 20000}},");
        sb.append("'series':{'terms':{'field':'taxonomies.series', 'size': 20000}},");
        sb.append("'species':{'terms':{'field':'taxonomies.species', 'size': 20000}},");
        sb.append("'species_group':{'terms':{'field':'taxonomies.species_group', 'size': 20000}},");
        sb.append("'species_subgroup':{'terms':{'field':'taxonomies.species_subgroup', 'size': 20000}},");
        sb.append("'subclass':{'terms':{'field':'taxonomies.subclass', 'size': 20000}},");
        sb.append("'subcohort':{'terms':{'field':'taxonomies.subcohort', 'size': 20000}},");
        sb.append("'subfamily':{'terms':{'field':'taxonomies.subfamily', 'size': 20000}},");
        sb.append("'subgenus':{'terms':{'field':'taxonomies.subgenus', 'size': 20000}},");
        sb.append("'subkingdom':{'terms':{'field':'taxonomies.subkingdom', 'size': 20000}},");
        sb.append("'suborder':{'terms':{'field':'taxonomies.suborder', 'size': 20000}},");
        sb.append("'subphylum':{'terms':{'field':'taxonomies.subphylum', 'size': 20000}},");
        sb.append("'subsection':{'terms':{'field':'taxonomies.subsection', 'size': 20000}},");
        sb.append("'subspecies':{'terms':{'field':'taxonomies.subspecies', 'size': 20000}},");
        sb.append("'subtribe':{'terms':{'field':'taxonomies.subtribe', 'size': 20000}},");
        sb.append("'superclass':{'terms':{'field':'taxonomies.superclass', 'size': 20000}},");
        sb.append("'superfamily':{'terms':{'field':'taxonomies.superfamily', 'size': 20000}},");
        sb.append("'superkingdom':{'terms':{'field':'taxonomies.superkingdom', 'size': 20000}},");
        sb.append("'superorder':{'terms':{'field':'taxonomies.superorder', 'size': 20000}},");
        sb.append("'superphylum':{'terms':{'field':'taxonomies.superphylum', 'size': 20000}},");
        sb.append("'tribe':{'terms':{'field':'taxonomies.tribe', 'size': 20000}},");
        sb.append("'varietas':{'terms':{'field':'taxonomies.varietas', 'size': 20000}}");
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