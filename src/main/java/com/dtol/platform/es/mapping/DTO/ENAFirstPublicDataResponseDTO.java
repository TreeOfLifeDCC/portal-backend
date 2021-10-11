package com.dtol.platform.es.mapping.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class ENAFirstPublicDataResponseDTO {
    private Long count;
    private String enaFirstPublic;
    private Date enaFirstPublicInDate;
}
