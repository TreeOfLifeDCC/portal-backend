package com.dtol.platform.es.mapping.DTO;

import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

public class OrganismExperimentDTO {
    @Field(type = FieldType.Keyword)
    private String study_accession;
    @Field(type = FieldType.Keyword)
    private String sample_accession;
    @Field(type = FieldType.Keyword)
    private String experiment_accession;
    @Field(type = FieldType.Keyword)
    private String run_accession;
    @Field(type = FieldType.Keyword)
    private String tax_id;
    @Field(type = FieldType.Keyword)
    private String scientific_name;
    @Field(type = FieldType.Keyword)
    private List<String> fastq_ftp;
    @Field(type = FieldType.Keyword)
    private List<String> submitted_ftp;
    @Field(type = FieldType.Keyword)
    private List<String> sra_ftp;
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time, pattern = "yyyy-MM-dd")
    private String first_public;
    @Field(type = FieldType.Keyword)
    private String instrument_model;
    @Field(type = FieldType.Keyword)
    private String instrument_platform;
    @Field(type = FieldType.Keyword)
    private String library_layout;
    @Field(type = FieldType.Keyword)
    private String library_selection;
    @Field(type = FieldType.Keyword)
    private String library_source;
    @Field(type = FieldType.Keyword)
    private String library_strategy;
    @Field(type = FieldType.Keyword)
    private String library_construction_protocol;

    public String getStudy_accession() {
        return study_accession;
    }

    public void setStudy_accession(String study_accession) {
        this.study_accession = study_accession;
    }

    public String getSample_accession() {
        return sample_accession;
    }

    public void setSample_accession(String sample_accession) {
        this.sample_accession = sample_accession;
    }

    public String getExperiment_accession() {
        return experiment_accession;
    }

    public void setExperiment_accession(String experiment_accession) {
        this.experiment_accession = experiment_accession;
    }

    public String getRun_accession() {
        return run_accession;
    }

    public void setRun_accession(String run_accession) {
        this.run_accession = run_accession;
    }

    public String getTax_id() {
        return tax_id;
    }

    public void setTax_id(String tax_id) {
        this.tax_id = tax_id;
    }

    public String getScientific_name() {
        return scientific_name;
    }

    public void setScientific_name(String scientific_name) {
        this.scientific_name = scientific_name;
    }

    public List<String> getFastq_ftp() {
        return fastq_ftp;
    }

    public void setFastq_ftp(List<String> fastq_ftp) {
        this.fastq_ftp = fastq_ftp;
    }

    public List<String> getSubmitted_ftp() {
        return submitted_ftp;
    }

    public void setSubmitted_ftp(List<String> submitted_ftp) {
        this.submitted_ftp = submitted_ftp;
    }

    public List<String> getSra_ftp() {
        return sra_ftp;
    }

    public void setSra_ftp(List<String> sra_ftp) {
        this.sra_ftp = sra_ftp;
    }

    public String getFirst_public() {
        return first_public;
    }

    public void setFirst_public(String first_public) {
        this.first_public = first_public;
    }

    public String getInstrument_model() {
        return instrument_model;
    }

    public void setInstrument_model(String instrument_model) {
        this.instrument_model = instrument_model;
    }

    public String getInstrument_platform() {
        return instrument_platform;
    }

    public void setInstrument_platform(String instrument_platform) {
        this.instrument_platform = instrument_platform;
    }

    public String getLibrary_layout() {
        return library_layout;
    }

    public void setLibrary_layout(String library_layout) {
        this.library_layout = library_layout;
    }

    public String getLibrary_selection() {
        return library_selection;
    }

    public void setLibrary_selection(String library_selection) {
        this.library_selection = library_selection;
    }

    public String getLibrary_source() {
        return library_source;
    }

    public void setLibrary_source(String library_source) {
        this.library_source = library_source;
    }

    public String getLibrary_strategy() {
        return library_strategy;
    }

    public void setLibrary_strategy(String library_strategy) {
        this.library_strategy = library_strategy;
    }

    public String getLibrary_construction_protocol() {
        return library_construction_protocol;
    }

    public void setLibrary_construction_protocol(String library_construction_protocol) {
        this.library_construction_protocol = library_construction_protocol;
    }
}
