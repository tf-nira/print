package io.mosip.print.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class FieldDTO {

    private String id;
    private List<String> fields;
    private String source;
    private String process;
    private Boolean bypassCache;
}
