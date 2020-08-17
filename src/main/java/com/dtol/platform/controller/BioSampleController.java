package com.dtol.platform.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dtol")
public class BioSampleController {

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String findBioSampleById() {
        return "Welcome to Darwin Tree of Life Project";
    }
}
