/*
 * Generated by Cadeau Support.
 *
 * https://github.com/spldeolin/cadeau-support
 */

package ${inputPackage};

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.*;
import javax.validation.constraints.*;
import org.springframework.beans.BeanUtils;
import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;
import ${textOption};
import ${modelPackage}.${model};

/**
 * ${description}
${classDocEnd}
@Data
@Accessors(chain = true)
public class ${model}Input implements Serializable {

	<#list fields as field>
<#if field.javadoc ??>
<#if field.javadoc ? length gt 1>
    /**
     * ${field.javadoc}
     */
</#if>
</#if>
<#list field.invalidAnnotations as invalidAnnotation>
    ${invalidAnnotation}
</#list>
    private ${field.type} ${field.name};

	</#list>
	private static final long serialVersionUID = 1L;

    public ${model} toModel() {
        return ${model}.builder()<#list fields as field>.${field.name}(${field.name})</#list>.build();
    }

}