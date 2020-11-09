package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.service.OrganismService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/organisms")
public class OrganismController {

    @Autowired
    OrganismService organismService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public HashMap<String, Object> getBioSample(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        HashMap<String, Object> response =new HashMap<>();
        List<Organism> resp = organismService.findAll(offset, limit);
        long count = organismService.getBiosampleCount();
        response.put("biosamples",resp);
        response.put("count",count);
        return response;

    }

    @RequestMapping(value = "/{accession}", method = RequestMethod.GET)
    public Organism findBioSampleByAccession(@PathVariable("accession") String accession) {
        return organismService.findBioSampleByAccession(accession);
    }

    @RequestMapping(value = "/organism/{name}", method = RequestMethod.GET)
    public Organism findBioSampleByOrganism(@PathVariable("name") String name) {
        return organismService.findBioSampleByOrganism(name);
    }

    @RequestMapping(value = "/sample", method = RequestMethod.POST)
    public String saveBioSample(@RequestBody Organism organism) {
        return organismService.saveBioSample(organism);
    }

}
