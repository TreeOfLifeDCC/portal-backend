package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.BioSample;
import com.dtol.platform.es.service.BioSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/dtol")
public class BioSampleController {

    @Autowired
    BioSampleService bioSampleService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public HashMap<String, Object> findBioSampleByAccession(@RequestParam("page") int page, @RequestParam("size") int size) {
        HashMap<String, Object> response =new HashMap<>();
        List<BioSample> resp = bioSampleService.findAll(page, size);
        response.put("biosamples",resp);
        response.put("count",resp.size());
        return response;

    }

    @RequestMapping(value = "/{accession}", method = RequestMethod.GET)
    public BioSample findBioSampleByAccession(@PathVariable("accession") String accession) {
        return bioSampleService.findBioSampleByAccession(accession);
    }

    @RequestMapping(value = "/sample/{name}", method = RequestMethod.GET)
    public BioSample findBioSampleByScientificName(@PathVariable("name") String name) {
        return bioSampleService.findBioSampleByScientificName(name);
    }

    @RequestMapping(value = "/sample", method = RequestMethod.POST)
    public String saveBioSample(@RequestBody BioSample bioSample) {
        return bioSampleService.saveBioSample(bioSample);
    }

}
