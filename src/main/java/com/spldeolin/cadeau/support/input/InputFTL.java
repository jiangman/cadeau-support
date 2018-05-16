package com.spldeolin.cadeau.support.input;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class InputFTL implements Comparable<InputFTL> {

    private int index;

    private String inputPackage;

    private String textOption;

    private String description;

    private String author;

    private String date;

    private String classDocEnd;

    private String modelPackage;

    private String model;

    private List<DtoFieldFTL> fields;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputFTL inputTemplate = (InputFTL) o;
        return model.equals(inputTemplate.model);
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }

    @Override
    public int compareTo(InputFTL that) {
        int thatIndex = that.getIndex();
        return Integer.compare(this.index, thatIndex);
    }

}
