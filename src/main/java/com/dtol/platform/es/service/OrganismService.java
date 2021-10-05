package com.dtol.platform.es.service;

import com.dtol.platform.es.mapping.DTO.GeoLocationDTO;
import com.dtol.platform.es.mapping.SecondaryOrganism;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrganismService {

    List<SecondaryOrganism> findAll(int page, int size, Optional<String> sortColumn, Optional<String> sortOrder);

    SecondaryOrganism findBioSampleByAccession(String accession);

    String saveBioSample(SecondaryOrganism secondaryOrganism);

    long getBiosampleCount();

    SecondaryOrganism findBioSampleByOrganismByText(String organism);

    Map<String, JSONArray> getSpecimensFilters(String accession) throws ParseException;

    String getOrganismByAccession(String accession);

    String getSpecimenByAccession(String accession);

    ArrayList<GeoLocationDTO> getOrganismsLocations();

    Map<String, List<JSONObject>> getCountOrganismParts();

    Map<String, List<JSONObject>> getFirstPublicCount();
}
