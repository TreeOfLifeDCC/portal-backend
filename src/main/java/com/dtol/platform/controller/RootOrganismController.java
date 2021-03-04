package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.RootOrganism;
import com.dtol.platform.es.service.RootSampleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation(value = "View a list of Root Organisms", response = Iterable.class)
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<String, Object>> getAllRootOrganisms(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                                       @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                                                       @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                                       @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        HashMap<String, Object> response = new HashMap<>();
        List<RootOrganism> resp = rootSampleService.findAllOrganisms(offset, limit, sortColumn, sortOrder);
        long count = rootSampleService.getRootOrganismCount();
        response.put("rootSamples", resp);
        response.put("count", count);
        return new ResponseEntity<HashMap<String, Object>>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Root Organism By Name", response = Iterable.class)
    @RequestMapping(value = "/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RootOrganism> getRootOrganismByName(@PathVariable("name") String name) {
        RootOrganism rs = rootSampleService.findRootSampleByOrganism(name);
        return new ResponseEntity<RootOrganism>(rs, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Filters for Filtering Root Organisms", response = Iterable.class)
    @RequestMapping(value = "/root/filters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<JSONObject>>> getRootOrganismFilters() throws ParseException {
        Map<String, List<JSONObject>> resp = rootSampleService.getRootOrganismFilters();
        return new ResponseEntity<Map<String, List<JSONObject>>>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Filters for Filtering Secondary Organisms", response = Iterable.class)
    @RequestMapping(value = "/secondary/filters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, JSONArray>> getSecondaryOrganismFilters(@RequestParam(name = "organism") String organism) throws ParseException {
        Map<String, JSONArray> resp = rootSampleService.getSecondaryOrganismFilters(organism);
        return new ResponseEntity<Map<String, JSONArray>>(resp, HttpStatus.OK);
    }


    @ApiOperation(value = "Get Filtered Results for Root Organisms", response = Iterable.class)
    @RequestMapping(value = "/root/filter/results", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getFilteredRootOrganisms(@RequestBody String filter,
                                                           @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                           @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                           @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                           @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        String resp = rootSampleService.findRootOrganismFilterResults(filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Root Organism Search Results", response = Iterable.class)
    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findSearchResults(@RequestParam("filter") String filter,
                                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {

        String resp = rootSampleService.findRootOrganismSearchResult(filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

}