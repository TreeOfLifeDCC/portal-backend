package com.dtol.platform.es.mapping;

import com.dtol.platform.es.mapping.DTO.BioSampleExperimentDTO;
import org.springframework.data.annotation.Id;
import com.dtol.platform.es.mapping.DTO.BioSampleCustomFieldDTO;
import com.dtol.platform.es.mapping.DTO.BioSampleOntologyDTO;
import com.dtol.platform.es.mapping.DTO.BioSampleRelationDTO;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.List;

@Document(indexName = "dtol", createIndex = false, replicas = 2, shards = 1)
public class BioSample {

    @Id
    private String Id;

    @Field(name="accession", type = FieldType.Keyword)
    private String accession;

    @Field(name="taxonId", type = FieldType.Keyword )
    private String taxonId;

    @Field(name="scientificName", type = FieldType.Keyword)
    private String scientificName;

    @Field(name="specimenId", type = FieldType.Keyword)
    private String specimenId;

    @Field(name="cultureOrStrainId", type = FieldType.Text)
    private String cultureOrStrainId;

    @Field(name="lifeStage", type = FieldType.Text)
    private String lifeStage;

    @Field(name="sex", type = FieldType.Nested)
    private List<BioSampleOntologyDTO> sex;

    @Field(name="relationship", type = FieldType.Nested)
    private List<BioSampleRelationDTO> relationship;

    @Field(name="gal", type = FieldType.Text)
    private String gal;

    @Field(name="galSampleId", type = FieldType.Keyword)
    private String galSampleId;

    @Field(name="collectedBy", type = FieldType.Text)
    private String collectedBy;

    @Field(name="collectingInstitution", type = FieldType.Text)
    private String collectingInstitution;

    @Field(name="collectionDate", type = FieldType.Date, format = DateFormat.basic_date_time, pattern = "yyyy-MM-dd")
    private LocalDate collectionDate;

    @Field(name="geographicLocationCountry", type = FieldType.Text)
    private String geographicLocationCountry;

    @Field(name="geographicLocationRegion", type = FieldType.Text)
    private String geographicLocationRegion;

    @Field(name="geographicLocationLatitude", type = FieldType.Text)
    private Double geographicLocationLatitude;

    @Field(name="geographicLocationLongitude", type = FieldType.Text)
    private Double geographicLocationLongitude;

    @Field(name="habitat", type = FieldType.Text)
    private String habitat;

    @Field(name="geographicLocationDepth", type = FieldType.Text)
    private Double geographicLocationDepth;

    @Field(name="geographicLocationElevation", type = FieldType.Text)
    private Double geographicLocationElevation;

    @Field(name="identifiedBy", type = FieldType.Text)
    private String identifiedBy;

    @Field(name="identifierAffiliation", type = FieldType.Text)
    private String identifierAffiliation;

    @Field(name="specimenVoucher", type = FieldType.Text)
    private String specimenVoucher;

    @Field(name="projectName", type = FieldType.Keyword)
    private String projectName;

    @Field(name="customField", type = FieldType.Nested)
    private List<BioSampleCustomFieldDTO> customField;

    @Field(name="experiment", type = FieldType.Nested)
    private List<BioSampleExperimentDTO> experiment;

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getTaxonId() {
        return taxonId;
    }

    public void setTaxonId(String taxonId) {
        this.taxonId = taxonId;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getSpecimenId() {
        return specimenId;
    }

    public void setSpecimenId(String specimenId) {
        this.specimenId = specimenId;
    }

    public String getCultureOrStrainId() {
        return cultureOrStrainId;
    }

    public void setCultureOrStrainId(String cultureOrStrainId) {
        this.cultureOrStrainId = cultureOrStrainId;
    }

    public String getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(String lifeStage) {
        this.lifeStage = lifeStage;
    }

    public List<BioSampleOntologyDTO> getSex() {
        return sex;
    }

    public void setSex(List<BioSampleOntologyDTO> sex) {
        this.sex = sex;
    }

    public List<BioSampleRelationDTO> getRelationship() {
        return relationship;
    }

    public void setRelationship(List<BioSampleRelationDTO> relationship) {
        this.relationship = relationship;
    }

    public String getGal() {
        return gal;
    }

    public void setGal(String gal) {
        this.gal = gal;
    }

    public String getGalSampleId() {
        return galSampleId;
    }

    public void setGalSampleId(String galSampleId) {
        this.galSampleId = galSampleId;
    }

    public String getCollectedBy() {
        return collectedBy;
    }

    public void setCollectedBy(String collectedBy) {
        this.collectedBy = collectedBy;
    }

    public String getCollectingInstitution() {
        return collectingInstitution;
    }

    public void setCollectingInstitution(String collectingInstitution) {
        this.collectingInstitution = collectingInstitution;
    }

    public LocalDate getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDate collectionDate) {
        this.collectionDate = collectionDate;
    }

    public String getGeographicLocationCountry() {
        return geographicLocationCountry;
    }

    public void setGeographicLocationCountry(String geographicLocationCountry) {
        this.geographicLocationCountry = geographicLocationCountry;
    }

    public String getGeographicLocationRegion() {
        return geographicLocationRegion;
    }

    public void setGeographicLocationRegion(String geographicLocationRegion) {
        this.geographicLocationRegion = geographicLocationRegion;
    }

    public Double getGeographicLocationLatitude() {
        return geographicLocationLatitude;
    }

    public void setGeographicLocationLatitude(Double geographicLocationLatitude) {
        this.geographicLocationLatitude = geographicLocationLatitude;
    }

    public Double getGeographicLocationLongitude() {
        return geographicLocationLongitude;
    }

    public void setGeographicLocationLongitude(Double geographicLocationLongitude) {
        this.geographicLocationLongitude = geographicLocationLongitude;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public Double getGeographicLocationDepth() {
        return geographicLocationDepth;
    }

    public void setGeographicLocationDepth(Double geographicLocationDepth) {
        this.geographicLocationDepth = geographicLocationDepth;
    }

    public Double getGeographicLocationElevation() {
        return geographicLocationElevation;
    }

    public void setGeographicLocationElevation(Double geographicLocationElevation) {
        this.geographicLocationElevation = geographicLocationElevation;
    }

    public String getIdentifiedBy() {
        return identifiedBy;
    }

    public void setIdentifiedBy(String identifiedBy) {
        this.identifiedBy = identifiedBy;
    }

    public String getIdentifierAffiliation() {
        return identifierAffiliation;
    }

    public void setIdentifierAffiliation(String identifierAffiliation) {
        this.identifierAffiliation = identifierAffiliation;
    }

    public String getSpecimenVoucher() {
        return specimenVoucher;
    }

    public void setSpecimenVoucher(String specimenVoucher) {
        this.specimenVoucher = specimenVoucher;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<BioSampleCustomFieldDTO> getCustomField() {
        return customField;
    }

    public void setCustomField(List<BioSampleCustomFieldDTO> customField) {
        this.customField = customField;
    }

    public List<BioSampleExperimentDTO> getExperiment() {
        return experiment;
    }

    public void setExperiment(List<BioSampleExperimentDTO> experiment) {
        this.experiment = experiment;
    }
}
