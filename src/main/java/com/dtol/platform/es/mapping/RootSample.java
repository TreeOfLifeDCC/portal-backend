package com.dtol.platform.es.mapping;

import com.dtol.platform.es.mapping.DTO.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "root_samples", createIndex = false, replicas = 2, shards = 1)
public class RootSample {

    @Id
    String id;

    @Field(name = "accession", type = FieldType.Keyword)
    private String accession;

    @Field(name = "commonName", type = FieldType.Keyword)
    private String commonName;

    @Field(name = "organism", type = FieldType.Keyword)
    private String organism;

    @Field(name = "sex", type = FieldType.Keyword)
    private String sex;

    @Field(name = "trackingSystem", type = FieldType.Keyword)
    private String trackingSystem;

    @Field(name = "customFields", type = FieldType.Nested)
    private List<OrganismCustomFieldsDTO> customFields;

    @Field(name = "records", type = FieldType.Nested)
    private List<RootSampleRecordsDTO> records;

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getTrackingSystem() {
        return trackingSystem;
    }

    public void setTrackingSystem(String trackingSystem) {
        this.trackingSystem = trackingSystem;
    }

    public List<OrganismCustomFieldsDTO> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<OrganismCustomFieldsDTO> customFields) {
        this.customFields = customFields;
    }

    public List<RootSampleRecordsDTO> getRecords() {
        return records;
    }

    public void setRecords(List<RootSampleRecordsDTO> records) {
        this.records = records;
    }
}
