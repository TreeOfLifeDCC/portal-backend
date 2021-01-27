package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.DTO.RootOrganism;
import com.dtol.platform.es.service.RootSampleService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/root_organisms")
public class RootOrganismController {

    @Autowired
    RootSampleService rootSampleService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<HashMap<String, Object>> getDistinctOrganisms(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
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

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public ResponseEntity<RootOrganism> findRootSampleByOrganism(@PathVariable("name") String name) {
        RootOrganism rs = rootSampleService.findRootSampleByOrganism(name);
        return new ResponseEntity<RootOrganism>(rs, HttpStatus.OK);
    }

    @RequestMapping(value = "/root/filters", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<JSONObject>>> getRootFilters() throws ParseException {
        Map<String, List<JSONObject>> resp = rootSampleService.getRootOrganismFilters();
        return new ResponseEntity<Map<String, List<JSONObject>>>(resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/filters", method = RequestMethod.GET)
    public ResponseEntity<Map<String, JSONArray>> getFilters(@RequestParam(name = "organism") String organism) throws ParseException {
        Map<String, JSONArray> resp = rootSampleService.getSecondaryOrganismFilters(organism);
        return new ResponseEntity<Map<String, JSONArray>>(resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/filter/results", method = RequestMethod.POST)
    public ResponseEntity<String> findFilterResults(@RequestBody String filter,
                                                    @RequestParam(name = "organism") String organism,
                                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        String resp = rootSampleService.findSecondaryOrganismFilterResults(organism, filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/root/filter/results", method = RequestMethod.POST)
    public ResponseEntity<String> findRootFilterResults(@RequestBody String filter,
                                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        String resp = rootSampleService.findRootOrganismFilterResults(filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseEntity<String> findSearchResults(@RequestParam("filter") String filter,
                                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {

        String resp = rootSampleService.findRootOrganismSearchResult(filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

}
