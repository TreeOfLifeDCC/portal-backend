package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.OrganismStatusTracking;
import com.dtol.platform.es.service.OrganismStatusTrackingService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
    public HashMap<String, Object> getBioSampleStatusTracking(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                              @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        HashMap<String, Object> response = new HashMap<>();
        List<OrganismStatusTracking> resp = organismStatusTrackingService.findAll(offset, limit);
        long count = organismStatusTrackingService.getBiosampleStatusTrackingCount();
        response.put("biosampleStatus", resp);
        response.put("count", count);
        return response;

    }

    @RequestMapping(value = "/filters", method = RequestMethod.GET)
    public Map<String, List<JSONObject>> getFilters() {
        return organismStatusTrackingService.getFilters();
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String findSearchResults(@RequestParam("filter") String filter,
                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        return organismStatusTrackingService.findSearchResult(filter, from, size, sortColumn, sortOrder);
    }

    @RequestMapping(value = "/filter/results", method = RequestMethod.POST)
    public String findFilterResults(@RequestBody String filter,
                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        return organismStatusTrackingService.findFilterResults(filter, from, size, sortColumn, sortOrder);
    }
}
