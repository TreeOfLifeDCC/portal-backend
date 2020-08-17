package com.dtol.platform.es.mapping;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "dtol", createIndex = true, replicas = 2, shards = 1)
public class BioSample {

    @Field(name="id", type = FieldType.Keyword)
    private String id;

    @Field(name="name", type = FieldType.Keyword)
    private String name;

    @Field(name="status", type = FieldType.Text)
    private String status;

    @Field(name="description", type = FieldType.Text)
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
