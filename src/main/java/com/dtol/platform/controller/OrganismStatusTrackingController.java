package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.OrganismStatusTracking;
import com.dtol.platform.es.service.OrganismStatusTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/statuses")
public class OrganismStatusTrackingController {

    @Autowired
    OrganismStatusTrackingService organismStatusTrackingService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public HashMap<String, Object> getBioSampleStatusTracking(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                              @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        HashMap<String, Object> response = new HashMap<>();
        List<OrganismStatusTracking> resp = organismStatusTrackingService.findAll(offset, limit);
        long count = organismStatusTrackingService.getBiosampleStatusTrackingCount();
        response.put("biosampleStatus", resp);
        response.put("count", count);
        return response;

    }
}
