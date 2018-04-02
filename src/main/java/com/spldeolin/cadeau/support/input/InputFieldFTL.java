package com.spldeolin.cadeau.support.input;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InputFieldFTL {

    private String javadoc;

    private String type;

    private String name;

    private String nameSnake;

    private List<String> invalidAnnotations;

}
