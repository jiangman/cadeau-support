### 描述

- ${commonDesc}

### URL

- `${httpUrl}`

### 动词

- ${httpMethod}

### 参数说明

<#if !paramShow>

- 无需参数

</#if>
<#if paramShow>
|名称|必传|JSON类型|描述|
|-|
<#list paramFields as paramField>
|${paramField.paramName}|${paramField.paramRequired}|${paramField.paramType}|${paramField.paramDesc}|
</#list>
<br>

<#if paramBodyShow>
### 请求体示例

```json
${paramBodyJson}
```
<br>

### 请求体说明

|名称|JSON类型|描述|
|-|
<#list paramBodyFields as bodyField>
|${bodyField.bodyName}|${bodyField.bodyType}|${bodyField.bodyDesc}|
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
</#if>

<#if returnShow>
<#if !isRetrunSimpleType>
### 返回值说明

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