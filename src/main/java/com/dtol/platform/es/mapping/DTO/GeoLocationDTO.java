package com.dtol.platform.es.mapping.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Builder
public class GeoLocationDTO {

    private String type;
    private String id;
    private ArrayList<Double> coordinates;


}
