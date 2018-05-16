package com.spldeolin.cadeau.support.input;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

@Data
public class DTOFieldFTLHolder {

    private static final DTOFieldFTLHolder instance = new DTOFieldFTLHolder();

    private Map<String, DtoFieldFTL> fields = new ConcurrentHashMap<>();

    public static DTOFieldFTLHolder getInstance() {
        return instance;
    }

    private DTOFieldFTLHolder() { }

}
