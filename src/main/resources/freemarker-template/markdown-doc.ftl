### 描述

- ${commonDesc}

### URL

- `${httpUrl}`

### 动词

- ${httpMethod}

<#if paramShow>
### 参数说明

|名称|JSON类型|必传|描述|
|-|
<#list paramFields as paramField>
|${paramField.paramName}|${paramField.paramType}|${paramField.paramRequired}|${paramField.paramDesc}|
</#list>
<br>
</#if>

<#if bodyShow>
### 请求体示例

```json
${bodyJson}
```
<br>

### 请求体说明

<#if !isBodySimpleType>
|名称|JSON类型|必传|描述|
|-|
<#list bodyFields as bodyField>
|${bodyField.bodyName}|${bodyField.bodyType}|${bodyField.bodyRequired}|${bodyField.bodyDesc}|
</#list>
<br>
</#if>
</#if>

### 返回值示例

<#if !returnShow>
- 没有返回值
</#if>
<#if returnShow>
```json
${returnJson}
```
<br>

### 返回值说明
<#if returnDesc ??>
${returnDesc}
</#if>

<#if !isRetrunSimpleType>
|名称|JSON类型|描述|
|-|
<#list returnFields as returnField>
|${returnField.returnName}|${returnField.returnType}|${returnField.returnDesc}|
</#list>
<br>
</#if>
</#if>

### 开发者

- ${commonDeveloper}