package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.BioSampleStatusTracking;

import java.util.List;

public interface BioSampleStatusTrackingService {

    public List<BioSampleStatusTracking> findAll(int page, int size);

    public long getBiosampleStatusTrackingCount();
}
