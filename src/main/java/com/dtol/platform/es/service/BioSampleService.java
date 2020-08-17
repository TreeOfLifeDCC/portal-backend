package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.BioSample;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface BioSampleService {

    public BioSample findBioSampleById(String id);

    public BioSample findBioSampleByName(String name);

    public String saveBioSample(BioSample bioSample);
}
