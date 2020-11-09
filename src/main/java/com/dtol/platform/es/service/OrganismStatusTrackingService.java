package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.OrganismStatusTracking;

import java.util.List;

public interface OrganismStatusTrackingService {

    public List<OrganismStatusTracking> findAll(int page, int size);

    public long getBiosampleStatusTrackingCount();
}
