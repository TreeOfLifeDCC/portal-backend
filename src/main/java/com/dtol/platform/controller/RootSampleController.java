package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.RootSample;
import com.dtol.platform.es.service.RootSampleService;
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
@RequestMapping("/root_samples")
public class RootSampleController {

    @Autowired
    RootSampleService rootSampleService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<HashMap<String, Object>> getBioSample(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                                @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                                                @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                                @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        HashMap<String, Object> response = new HashMap<>();
        List<RootSample> resp = rootSampleService.findAll(offset, limit, sortColumn, sortOrder);
        long count = rootSampleService.getRootSamplesCount();
        response.put("rootSamples", resp);
        response.put("count", count);
        return new ResponseEntity<HashMap<String, Object>>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity<HashMap<String, Object>> getRootOrganisms(@RequestParam(name = "size", required = false, defaultValue = "20") String size,
                                                                @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                                @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder,
                                                                @RequestParam(value = "afterKey", required = false) Optional<String> afterKey) throws ParseException {
        HashMap<String, Object> response = new HashMap<>();
        JSONObject resp = rootSampleService.getDistinctRootSamplesByOrganism(size, sortColumn, sortOrder, afterKey);
        String count = rootSampleService.getDistinctRootSamplesCountByOrganism();
        response.put("rootSamples", resp);
        response.put("count", count);
        System.out.println(resp);
        return new ResponseEntity<HashMap<String, Object>>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/organism/{name}", method = RequestMethod.GET)
    public ResponseEntity<RootSample> findRootSampleByOrganism(@PathVariable("name") String name) {
        RootSample rs = rootSampleService.findRootSampleByOrganism(name);
        return new ResponseEntity<RootSample>(rs, HttpStatus.OK);
    }

    @RequestMapping(value = "/{accession}", method = RequestMethod.GET)
    public ResponseEntity<RootSample> findRootSampleByAccession(@PathVariable("accession") String accession) {
        RootSample rs = rootSampleService.findRootSampleByAccession(accession);
        return new ResponseEntity<RootSample>(rs, HttpStatus.OK);
    }

    @RequestMapping(value = "/filters", method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<JSONObject>>> getFilters() {
        Map<String, List<JSONObject>> resp = rootSampleService.getFilters();
        return new ResponseEntity<Map<String, List<JSONObject>>>(resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseEntity<String> findSearchResults(@RequestParam("filter") String filter,
                                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {

        String resp = rootSampleService.findSearchResult(filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/filter/results", method = RequestMethod.POST)
    public ResponseEntity<String> findFilterResults(@RequestBody String filter,
                                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        String resp = rootSampleService.findFilterResults(filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }
}
