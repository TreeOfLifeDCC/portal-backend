package com.dtol.platform.controller;

import com.dtol.platform.es.mapping.BioSampleStatusTracking;
import com.dtol.platform.es.service.BioSampleStatusTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/statuses")
public class BioSampleStatusTrackingController {

    @Autowired
    BioSampleStatusTrackingService bioSampleStatusTrackingService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public HashMap<String, Object> getBioSampleStatusTracking(@RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                              @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        HashMap<String, Object> response = new HashMap<>();
        List<BioSampleStatusTracking> resp = bioSampleStatusTrackingService.findAll(offset, limit);
        long count = bioSampleStatusTrackingService.getBiosampleStatusTrackingCount();
        response.put("biosampleStatus", resp);
        response.put("count", count);
        return response;

    }
}
