package com.spldeolin.cadeau.support.input;

import java.util.List;
import lombok.Data;

@Data
public class DtoFieldFTL {

    private String javadoc;

    private String type;

    private String name;

    private List<String> invalidAnnotations;

}
