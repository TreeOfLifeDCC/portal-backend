package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.OrganismStatusTracking;
import com.dtol.platform.es.mapping.RootSample;
import com.dtol.platform.es.service.OrganismStatusTrackingService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/statuses")
public class OrganismStatusTrackingController {

    @Autowired
    OrganismStatusTrackingService organismStatusTrackingService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<HashMap<String, Object>> getBioSampleStatusTracking(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                                              @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                                                              @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                                              @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        HashMap<String, Object> response = new HashMap<>();
        List<OrganismStatusTracking> resp = organismStatusTrackingService.findAll(offset, limit, sortColumn, sortOrder);
        long count = organismStatusTrackingService.getBiosampleStatusTrackingCount();
        response.put("biosampleStatus", resp);
        response.put("count", count);
        return new ResponseEntity<HashMap<String, Object>>(response, HttpStatus.OK);

    }

    @RequestMapping(value = "/filters", method = RequestMethod.GET)
    public Map<String, List<JSONObject>> getFilters() {
        return organismStatusTrackingService.getFilters();
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseEntity<String> findSearchResults(@RequestParam("filter") String filter,
                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {

        String resp = organismStatusTrackingService.findSearchResult(filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String> (resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/filter/results", method = RequestMethod.POST)
    public ResponseEntity<String> findFilterResults(@RequestBody String filter,
                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        String resp = organismStatusTrackingService.findFilterResults(filter, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String> (resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/organism", method = RequestMethod.GET)
    public ResponseEntity<String> findBioSampleByOrganism(@RequestParam("name") String name,
                                            @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                            @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                            @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                            @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        String resp = organismStatusTrackingService.findBioSampleByOrganismByText(name, from, size, sortColumn, sortOrder);
        return new ResponseEntity<String> (resp, HttpStatus.OK);
    }
}
