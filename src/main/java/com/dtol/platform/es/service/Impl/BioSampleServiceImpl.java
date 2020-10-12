package com.dtol.platform.es.service.Impl;

import com.dtol.platform.es.mapping.BioSample;
import com.dtol.platform.es.repository.BioSampleRepository;
import com.dtol.platform.es.service.BioSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BioSampleServiceImpl implements BioSampleService {

    @Autowired
    BioSampleRepository bioSampleRepository;

    @Override
    public List<BioSample> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BioSample> pageObj = bioSampleRepository.findAll(pageable);
        return pageObj.toList();
    }

    public BioSample findBioSampleByAccession(String accession) {
        BioSample bioSample = bioSampleRepository.findBioSampleByAccession(accession);
        return bioSample;
    }

    public BioSample findBioSampleByScientificName(String scientificName) {
        BioSample bioSample = bioSampleRepository.findByScientificName(scientificName);
        return bioSample;
    }

    public String saveBioSample(BioSample bioSample) {
        BioSample bs = bioSampleRepository.save(bioSample);
        return bs.getAccession();
    }

    public long countAll() {
        return bioSampleRepository.count();
    }


}
