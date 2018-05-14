package ${inputPackage};

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.*;
import javax.validation.constraints.*;
import org.springframework.beans.BeanUtils;
import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import ${textOption};
import ${modelPackage}.${model};

/**
 * ${description}
 *
 * @author ${author} ${date}
${generatorTag}
 */
@Data
public class ${model}Input implements Serializable {

	<#list fieldFileNames as fieldFileName>
        <#include "temp/${fieldFileName}.ftl"/>

	</#list>
	private static final long serialVersionUID = 1L;

    public ${model} toModel() {
        ${model} model = ${model}.builder().build();
        BeanUtils.copyProperties(this, model);
        return model;
    }

}