package com.dtol.platform.es.mapping.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // This will exclude null JSON attributes.
@JsonIgnoreProperties(ignoreUnknown = true)
// This will exclude any unknown(not available in the class) attribute in the JSON string.
public class GeoLocationResponseDTO {

    @JsonProperty("organism_id")
    private String Id;

    @JsonProperty("organismText")
    private String organismText;

    @JsonProperty("geographicLocationRegionAndLocality")
    private String geographicLocationRegionAndLocality;

    @JsonProperty("geographicLocationLatitude")
    private OrganismGeographicLocationDTO geographicLocationLatitude;

    @JsonProperty("geographicLocationLongitude")
    private OrganismGeographicLocationDTO geographicLocationLongitude;
}
