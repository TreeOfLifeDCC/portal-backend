package com.dtol.platform.es.mapping.DTO;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class AnnotationOtherDataDTO {

    @Field(type = FieldType.Keyword)
    String FTP_dumps;

    public String getFTP_dumps() {
        return FTP_dumps;
    }

    public void setFTP_dumps(String FTP_dumps) {
        this.FTP_dumps = FTP_dumps;
    }
}
