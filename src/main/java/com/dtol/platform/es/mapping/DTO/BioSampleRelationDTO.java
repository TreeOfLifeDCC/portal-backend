package com.dtol.platform.es.mapping.DTO;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class BioSampleRelationDTO {

    @Field(type = FieldType.Keyword)
    private String source;
    @Field(type = FieldType.Text)
    private String type;
    @Field(type = FieldType.Keyword)
    private String target;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
