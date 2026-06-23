package io.mosip.print.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryResponseDto {

    private String name;
    private String description;
    private List<CountryFieldVal> values;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountryFieldVal {
        private String code;
        private String value;
    }
}