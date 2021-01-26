package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.RootSample;
import com.dtol.platform.es.service.RootSampleService;
import org.json.simple.JSONArray;
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
public class RelatedSampleController {

    @Autowired
    RootSampleService rootSampleService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<HashMap<String, Object>> getBioSample(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                                @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                                                @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                                @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        HashMap<String, Object> response = new HashMap<>();
        List<RootSample> resp = rootSampleService.findAll(offset, limit, sortColumn, sortOrder);
        long count = rootSampleService.getRelatedOrganismCount();
        response.put("rootSamples", resp);
        response.put("count", count);
        return new ResponseEntity<HashMap<String, Object>>(response, HttpStatus.OK);
    }


    @RequestMapping(value = "/organism/{name}", method = RequestMethod.GET)
    public ResponseEntity<HashMap<String, Object>> findRootSampleByOrganism(@PathVariable("name") String name) {
        HashMap<String, Object> response = new HashMap<>();
        List<RootSample> rs = rootSampleService.findRelatedSampleByOrganism(name);
        response.put("organisms", rs);
        response.put("count", rs.size());
        return new ResponseEntity<HashMap<String, Object>>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/{accession}", method = RequestMethod.GET)
    public ResponseEntity<RootSample> findRootSampleByAccession(@PathVariable("accession") String accession) {
        RootSample rs = rootSampleService.findRootSampleByAccession(accession);
        return new ResponseEntity<RootSample>(rs, HttpStatus.OK);
    }

    @RequestMapping(value = "/filters", method = RequestMethod.GET)
    public ResponseEntity<Map<String, JSONArray>> getFilters(@RequestParam(name = "organism") String organism) throws ParseException {
        Map<String, JSONArray> resp = rootSampleService.getSecondaryOrganismFilters(organism);
        return new ResponseEntity<Map<String, JSONArray>>(resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/filter/results", method = RequestMethod.POST)
    public ResponseEntity<String> findFilterResults(@RequestBody String filter,
                                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        String resp = rootSampleService.findRelatedOrganismFilterResults(filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<String> saveBioSample(@RequestBody RootSample rootSample) {
        String resp = rootSampleService.saveRootSample(rootSample);
        return new ResponseEntity<String> (resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/organism", method = RequestMethod.GET)
    public ResponseEntity<JSONArray> findAccessionByOrganism(@RequestParam("organismName") String organismName) throws ParseException {
        JSONArray rs = rootSampleService.findSampleAccessionByOrganism(organismName);
        return new ResponseEntity<JSONArray>(rs, HttpStatus.OK);
    }
}
