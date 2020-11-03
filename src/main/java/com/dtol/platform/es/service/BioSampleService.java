package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.BioSample;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BioSampleService {

    public List<BioSample> findAll(int page, int size);

    public BioSample findBioSampleByAccession(String accession);

    public BioSample findBioSampleByOrganism(String organism);

    public String saveBioSample(BioSample bioSample);

    public long getBiosampleCount();
}
