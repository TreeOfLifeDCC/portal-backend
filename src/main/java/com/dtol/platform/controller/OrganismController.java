package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.BioSample;
import com.dtol.platform.es.service.BioSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/organisms")
public class BioSampleController {

    @Autowired
    BioSampleService bioSampleService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public HashMap<String, Object> getBioSample(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        HashMap<String, Object> response =new HashMap<>();
        List<BioSample> resp = bioSampleService.findAll(offset, limit);
        long count = bioSampleService.getBiosampleCount();
        response.put("biosamples",resp);
        response.put("count",count);
        return response;

    }

    @RequestMapping(value = "/{accession}", method = RequestMethod.GET)
    public BioSample findBioSampleByAccession(@PathVariable("accession") String accession) {
        return bioSampleService.findBioSampleByAccession(accession);
    }

    @RequestMapping(value = "/organism/{name}", method = RequestMethod.GET)
    public BioSample findBioSampleByOrganism(@PathVariable("name") String name) {
        return bioSampleService.findBioSampleByOrganism(name);
    }

    @RequestMapping(value = "/sample", method = RequestMethod.POST)
    public String saveBioSample(@RequestBody BioSample bioSample) {
        return bioSampleService.saveBioSample(bioSample);
    }

}
