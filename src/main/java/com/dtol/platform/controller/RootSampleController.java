package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.RootSample;
import com.dtol.platform.es.service.RootSampleService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
    public HashMap<String, Object> getBioSample(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                                @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        HashMap<String, Object> response = new HashMap<>();
        List<RootSample> resp = rootSampleService.findAll(offset, limit, sortColumn, sortOrder);
        long count = rootSampleService.getRootSamplesCount();
        response.put("rootSamples", resp);
        response.put("count", count);
        return response;

    }

    @RequestMapping(value = "/organism/{name}", method = RequestMethod.GET)
    public RootSample findRootSampleByOrganism(@PathVariable("name") String name) {
        return rootSampleService.findRootSampleByOrganism(name);
    }

    @RequestMapping(value = "/{accession}", method = RequestMethod.GET)
    public RootSample findRootSampleByAccession(@PathVariable("accession") String accession) {
        return rootSampleService.findRootSampleByAccession(accession);
    }

    @RequestMapping(value = "/filters", method = RequestMethod.GET)
    public Map<String, List<JSONObject>> getFilters() {
        return rootSampleService.getFilters();
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String findSearchResults(@RequestParam("filter") String filter,
                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        return rootSampleService.findSearchResult(filter, from, size, sortColumn, sortOrder);
    }

    @RequestMapping(value = "/filter/results", method = RequestMethod.POST)
    public String findFilterResults(@RequestBody String filter,
                                    @RequestParam(name = "from", required = false, defaultValue = "0") Optional<String> from,
                                    @RequestParam(value = "size", required = false, defaultValue = "20") Optional<String> size,
                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        return rootSampleService.findFilterResults(filter, from, size, sortColumn, sortOrder);
    }
}
