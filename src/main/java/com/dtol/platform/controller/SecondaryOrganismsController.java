package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.DTO.GeoLocationDTO;
import com.dtol.platform.es.mapping.SecondaryOrganism;
import com.dtol.platform.es.service.OrganismService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Hidden;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/organisms")
@Api(tags = "Secondary Organisms", description = "Controller for Secondary Organisms")
public class SecondaryOrganismsController {

    @Autowired
    OrganismService organismService;

    @ApiOperation(value = "Get Secondary Organism By Accession")
    @RequestMapping(value = "/{accession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSecondaryOrganismByAccession(@PathVariable("accession") String accession) {
        String rs = organismService.getOrganismByAccession(accession);
        return new ResponseEntity<String> (rs, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Filters for Secondary Organisms")
    @RequestMapping(value = "/filters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, JSONArray>> getSecondaryOrganismFilters(@RequestParam(name = "accession") String accession) throws ParseException {
        Map<String, JSONArray> resp = organismService.getSpecimensFilters(accession);
        return new ResponseEntity<Map<String, JSONArray>>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Specimen By Accession")
    @RequestMapping(value = "/specimen/{accession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSpecimenByAccession(@PathVariable("accession") String accession) {
        String rs = organismService.getSpecimenByAccession(accession);
        return new ResponseEntity<String> (rs, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Organisms geo location")
    @RequestMapping(value = "/geo-locations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<GeoLocationDTO> getOrganismsLocations() {
        return organismService.getOrganismsLocations();
    }

    @ApiOperation(value = "Get Organisms part count")
    @RequestMapping(value = "/count-organism-parts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<JSONObject>> getCountOrganismParts() {
        return organismService.getCountOrganismParts();
    }

    @ApiOperation(value = "Get Organisms part count")
    @RequestMapping(value = "/count-first-public", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<JSONObject>> getFirstPublicCount() {
        return organismService.getFirstPublicCount();
    }

}
