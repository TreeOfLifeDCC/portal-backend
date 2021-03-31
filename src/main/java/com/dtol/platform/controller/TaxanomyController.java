package com.dtol.platform.controller;

import com.dtol.platform.es.service.TaxanomyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/taxonomy")
@Api(tags = "Eukryota Taxonomies", description = "Controller for Taxonomy")
public class TaxanomyController {

    @Autowired
    TaxanomyService taxanomyService;

    @ApiOperation(value = "View a list of Eukaryota Taxanomies")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllTaxonomies(@RequestParam("type") String type) {
        String resp = taxanomyService.getAllTaxonomiesByType(type);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "View a list of Eukaryota child Taxanomies")
    @RequestMapping(value = "/{rank}/child", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getChildTaxonomyRank(@RequestParam("filter") Optional<String> filter,
                                                       @PathVariable("rank") String rank,
                                                       @RequestParam("taxonomy") String taxonomy,
                                                       @RequestParam("childRank") String childRank,
                                                       @RequestBody String taxaTree) throws ParseException {
        String resp = taxanomyService.getChildTaxonomyRank(filter, rank, taxonomy, childRank, taxaTree);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Taxonomy Filters for Root Organisms")
    @RequestMapping(value = "/filters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTaxonomyFilters(@RequestParam("taxonomy")Optional<String> taxonomy) throws ParseException {
        String resp = taxanomyService.getTaxonomicRanksAndCounts(taxonomy);
        return new ResponseEntity<String>(resp, HttpStatus.OK);
    }
}
