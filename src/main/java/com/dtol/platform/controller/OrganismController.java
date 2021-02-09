package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.Organism;
import com.dtol.platform.es.mapping.RootSample;
import com.dtol.platform.es.service.OrganismService;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<HashMap<String, Object>> getBioSample(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                                @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                                                @RequestParam(name = "sortColumn", required = false) Optional<String> sortColumn,
                                                                @RequestParam(value = "sortOrder", required = false) Optional<String> sortOrder) {
        HashMap<String, Object> response = new HashMap<>();
        List<Organism> resp = organismService.findAll(offset, limit, sortColumn, sortOrder);
        long count = organismService.getBiosampleCount();
        response.put("biosamples", resp);
        response.put("count", count);
        return new ResponseEntity<HashMap<String, Object>> (response, HttpStatus.OK);

    }

    @RequestMapping(value = "/{accession}", method = RequestMethod.GET)
    public ResponseEntity<String> findBioSampleByAccession(@PathVariable("accession") String accession) {
        String rs = organismService.getOrganismByAccession(accession);
        return new ResponseEntity<String> (rs, HttpStatus.OK);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<String> saveBioSample(@RequestBody Organism organism) {
        String resp = organismService.saveBioSample(organism);
        return new ResponseEntity<String> (resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/detail/{organism}", method = RequestMethod.GET)
    public ResponseEntity<Organism> findBioSampleByOrganism(@PathVariable("organism") String organism) {
        Organism resp = organismService.findBioSampleByOrganismByText(organism);
        return new ResponseEntity<Organism> (resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/filters", method = RequestMethod.GET)
    public ResponseEntity<Map<String, JSONArray>> getFilters(@RequestParam(name = "accession") String accession) throws ParseException {
        Map<String, JSONArray> resp = organismService.getSpecimensFilters(accession);
        return new ResponseEntity<Map<String, JSONArray>>(resp, HttpStatus.OK);
    }

    @RequestMapping(value = "/specimen/{accession}", method = RequestMethod.GET)
    public ResponseEntity<String> getSpecimenByAccession(@PathVariable("accession") String accession) {
        String rs = organismService.getSpecimenByAccession(accession);
        return new ResponseEntity<String> (rs, HttpStatus.OK);
    }

}
