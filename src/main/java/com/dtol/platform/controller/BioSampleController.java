package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.BioSample;
import com.dtol.platform.es.service.BioSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dtol")
public class BioSampleController {

    @Autowired
    BioSampleService bioSampleService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BioSample findBioSampleById(@PathVariable("id") String id) {
        return bioSampleService.findBioSampleById(id);
    }

    @RequestMapping(value = "/sample/{name}", method = RequestMethod.GET)
    public BioSample findBioSampleByName(@PathVariable("name") String name) {
        return bioSampleService.findBioSampleByName(name);
    }

    @RequestMapping(value = "/sample", method = RequestMethod.POST)
    public String saveBioSample(@RequestBody BioSample bioSample) {
        return bioSampleService.saveBioSample(bioSample);
    }

}
