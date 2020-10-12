package com.dtol.platform.es.mapping.DTO;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

public class BioSampleExperimentDTO {
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
}
