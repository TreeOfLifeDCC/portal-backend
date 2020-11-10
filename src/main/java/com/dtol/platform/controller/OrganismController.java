package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.service.OrganismService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/organisms")
public class OrganismController {

    @Autowired
    OrganismService organismService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public HashMap<String, Object> getBioSample(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                                @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        HashMap<String, Object> response = new HashMap<>();
        List<Organism> resp = organismService.findAll(offset, limit, sortColumn, sortOrder);
        long count = organismService.getBiosampleCount();
        response.put("biosamples", resp);
        response.put("count", count);
        return response;

    }

    @RequestMapping(value = "/{accession}", method = RequestMethod.GET)
    public Organism findBioSampleByAccession(@PathVariable("accession") String accession) {
        return organismService.findBioSampleByAccession(accession);
    }

    @RequestMapping(value = "/sample", method = RequestMethod.POST)
    public String saveBioSample(@RequestBody Organism organism) {
        return organismService.saveBioSample(organism);
    }

    @RequestMapping(value = "/filters", method = RequestMethod.GET)
    public Map<String, JSONObject> findBioSampleByOrganism() {
        return organismService.getFilterValues();
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public HashMap<String, Object> findSearchResult(@RequestParam("filter") String filter,
                                                    @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                    @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        return organismService.findSearchResult(filter, sortColumn, sortOrder);
    }

}
