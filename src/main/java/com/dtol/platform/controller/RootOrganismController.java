package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.RootOrganism;
import com.dtol.platform.es.service.RootSampleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/root_organisms")
@Api(tags = "Root Organisms", description = "Controller for Root Organisms")
public class RootOrganismController {

    @Autowired
    RootSampleService rootSampleService;

    @ApiOperation(value = "View a list of Root Organisms")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, Object>> getAllRootOrganisms(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                                       @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                                                       @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                                       @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) throws ParseException {
        HashMap<String, Object> response = new HashMap<>();
        JSONArray resp = rootSampleService.findAllOrganisms(offset, limit, sortColumn, sortOrder);
        long count = rootSampleService.getRootOrganismCount();
        response.put("rootSamples", resp);
        response.put("count", count);
        return new ResponseEntity<HashMap<String, Object>>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Root Organism By Name")
    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RootOrganism> getRootOrganismByName(@ApiParam(example = "Lutra lutra") @PathVariable("name") String name) {
        RootOrganism rs = rootSampleService.findRootSampleByOrganism(name);
        return new ResponseEntity<RootOrganism>(rs, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Filters for Filtering Root Organisms")
    @RequestMapping(value = "/root/filters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<JSONObject>>> getRootOrganismFilters() throws ParseException {
        Map<String, List<JSONObject>> resp = rootSampleService.getRootOrganismFilters();
        return new ResponseEntity<Map<String, List<JSONObject>>>(resp, HttpStatus.OK);
    }

    @ApiIgnore
    @ApiOperation(value = "Get Filters for Filtering Secondary Organisms")
    @RequestMapping(value = "/secondary/filters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, JSONArray>> getSecondaryOrganismFilters(@ApiParam(example = "Lutra lutra") @RequestParam(name = "organism") String organism) throws ParseException {
        Map<String, JSONArray> resp = rootSampleService.getSecondaryOrganismFilters(organism);
        return new ResponseEntity<Map<String, JSONArray>>(resp, HttpStatus.OK);
    }


    @ApiOperation(value = "Get Filtered Results for Root Organisms")
    @RequestMapping(value = "/root/filter/results", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFilteredRootOrganisms(@ApiParam(example = "Submitted to BioSamples") @RequestBody Optional<String> filter,
                                                           @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                           @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                           @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                           @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder,
                                                           @ApiParam(example = "Salmo") @RequestParam(value = "searchText", required = false) Optional<String> search,
                                                           @ApiParam(example = "[{\"rank\":\"superkingdom\",\"taxonomy\":\"Eukaryota\",\"childRank\":\"kingdom\"}]") @RequestParam(value = "taxonomyFilter", required = false) Optional<String> taxonomyFilter) throws ParseException {
        String resp = rootSampleService.findRootOrganismFilterResults(search, filter, from, size, sortColumn, sortOrder, taxonomyFilter);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Root Organism Search Results")
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findSearchResults(@ApiParam(example = "lutra") @RequestParam("filter") String filter,
                                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {

        String resp = rootSampleService.findRootOrganismSearchResult(filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Root Organism By Id")
    @RequestMapping(value = "/root", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> getRootOrganismById(@ApiParam(example = "Lutra lutra") @PathParam("id") String id) throws ParseException {
        JSONObject rs = rootSampleService.findRootSampleById(id);
        return new ResponseEntity<JSONObject>(rs, HttpStatus.OK);
    }

    @RequestMapping(value = "/data/csv", method = RequestMethod.POST)
    public ResponseEntity<Resource> getFile(@ApiParam(example = "Submitted to BioSamples") @RequestBody Optional<String> filter,
                                            @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                            @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                            @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                            @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder,
                                            @ApiParam(example = "Salmo") @RequestParam(value = "searchText", required = false) Optional<String> search,
                                            @ApiParam(example = "[{\"rank\":\"superkingdom\",\"taxonomy\":\"Eukaryota\",\"childRank\":\"kingdom\"}]") @RequestParam(value = "taxonomyFilter", required = false) Optional<String> taxonomyFilter) throws IOException, ParseException {
        String filename = "organisms-metadata.csv";
        InputStreamResource file = new InputStreamResource(rootSampleService.csvDownload(search, filter, from, size, sortColumn, sortOrder, taxonomyFilter));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }


    @ApiOperation(value = "Download assemblies csv Format")
    @RequestMapping(value = "/assemblies/csv", method = RequestMethod.POST)
    public ResponseEntity<Resource> getAssembliesCSVFils(@ApiParam(example = "Submitted to BioSamples") @RequestBody Optional<String> filter,
                                                         @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                         @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                         @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                         @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder,
                                                         @ApiParam(example = "Salmo") @RequestParam(value = "searchText", required = false) Optional<String> search,
                                                         @ApiParam(example = "[{\"rank\":\"superkingdom\",\"taxonomy\":\"Eukaryota\",\"childRank\":\"kingdom\"}]") @RequestParam(value = "taxonomyFilter", required = false) Optional<String> taxonomyFilter) throws IOException, ParseException {
        String filename = "assemblies.csv";
        InputStreamResource file = new InputStreamResource(rootSampleService.getAssembliesCSVFils(search, filter, from, size, sortColumn, sortOrder, taxonomyFilter));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    @ApiOperation(value = "Get GIS Data for Organisms & Specimens")
    @RequestMapping(value = "/gis", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONArray> getGisData() throws ParseException {
        HashMap<String, Object> response = new HashMap<>();
        JSONArray resp = rootSampleService.getGisData();
        return new ResponseEntity<JSONArray>(resp, HttpStatus.OK);
    }
}
