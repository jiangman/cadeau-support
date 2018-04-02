<#if javadoc ??>
<#if javadoc ? length gt 1>
    /**
     * ${javadoc}
     */
</#if>
</#if>
<#if nameSnake ??>
    @JsonProperty("${nameSnake}")
</#if>
<#list invalidAnnotations as invalidAnnotation>
    ${invalidAnnotation}
</#list>
    private ${type} ${name};
