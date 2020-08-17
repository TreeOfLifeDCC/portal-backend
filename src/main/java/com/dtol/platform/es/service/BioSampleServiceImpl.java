package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.BioSample;
import com.dtol.platform.es.repository.BioSampleRepository;
import com.dtol.platform.es.repository.BioSampleRepositoryInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BioSampleServiceImpl implements BioSampleService {

    @Autowired
    BioSampleRepository bioSampleRepository;

    @Autowired
    BioSampleRepositoryInterface bioSampleRepositoryInterface;

    public BioSample findBioSampleById(String id) {
        BioSample bioSample = bioSampleRepository.findById(id);
        return bioSample;
    }

    public BioSample findBioSampleByName(String name) {
        BioSample bioSample = bioSampleRepositoryInterface.findByName(name);
        return bioSample;
    }

    public String saveBioSample(BioSample bioSample) {
        String id = bioSampleRepository.save(bioSample);
        return id;
    }




}
