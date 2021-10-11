package com.dtol.platform.es.mapping;

import com.dtol.platform.es.mapping.DTO.OrganismGeographicLocationDTO;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@ApiModel(description = "Geolocation Organism Organism Model")
@Document(indexName = "geolocation_organism", createIndex = false, replicas = 2, shards = 1)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationOrganism {

    @org.springframework.data.annotation.Id
    private String Id;

    @Field(name = "organismText", type = FieldType.Text)
    private String organismText;

    @Field(name = "geographicLocationRegionAndLocality", type = FieldType.Text)
    private String geographicLocationRegionAndLocality;

    @Field(name = "geographicLocationLatitude", type = FieldType.Nested)
    private OrganismGeographicLocationDTO geographicLocationLatitude;

    @Field(name = "geographicLocationLongitude", type = FieldType.Nested)
    private OrganismGeographicLocationDTO geographicLocationLongitude;
}
