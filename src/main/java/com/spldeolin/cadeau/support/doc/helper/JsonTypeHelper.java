package com.spldeolin.cadeau.support.doc.helper;

import static com.spldeolin.cadeau.support.doc.SimpleType.BIGDECIMAL;
import static com.spldeolin.cadeau.support.doc.SimpleType.BOOLEAN;
import static com.spldeolin.cadeau.support.doc.SimpleType.BYTE;
import static com.spldeolin.cadeau.support.doc.SimpleType.CHAR;
import static com.spldeolin.cadeau.support.doc.SimpleType.CHARACTER;
import static com.spldeolin.cadeau.support.doc.SimpleType.DATE;
import static com.spldeolin.cadeau.support.doc.SimpleType.DOUBLE;
import static com.spldeolin.cadeau.support.doc.SimpleType.FLOAT;
import static com.spldeolin.cadeau.support.doc.SimpleType.INTEGER;
import static com.spldeolin.cadeau.support.doc.SimpleType.Int;
import static com.spldeolin.cadeau.support.doc.SimpleType.LOCALDATE;
import static com.spldeolin.cadeau.support.doc.SimpleType.LOCALDATETIME;
import static com.spldeolin.cadeau.support.doc.SimpleType.LOCALTIME;
import static com.spldeolin.cadeau.support.doc.SimpleType.LONG;
import static com.spldeolin.cadeau.support.doc.SimpleType.SHORT;
import static com.spldeolin.cadeau.support.doc.SimpleType.STRING;

import org.apache.commons.lang3.StringUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * String,                 // 字符串
 * Integer, Noninteger,    // 整数数字，非整数数字
 * Object,                 // 对象
 * Array,                  // 数组
 * Boolean                 // 布尔值
 *
 * @author Deolin 2018/06/16
 */
@UtilityClass
@Log4j2
public class JsonTypeHelper {

    public String getJsonTypeFromJavaSimpleType(String javaTypeName) {
        if (StringUtils.equalsAnyIgnoreCase(javaTypeName, BYTE.getName(), SHORT.getName(), INTEGER.getName(),
                LONG.getName(), DATE.getName(), Int.getName())) {
            return "Integer";
        }
        if (StringUtils.equalsAnyIgnoreCase(javaTypeName, FLOAT.getName(), DOUBLE.getName(), BIGDECIMAL.getName())) {
            return "Noninteger";
        }
        if (StringUtils.equalsAnyIgnoreCase(javaTypeName, BOOLEAN.getName())) {
            return "Boolean";
        }
        if (StringUtils.equalsAnyIgnoreCase(javaTypeName, CHARACTER.getName(), CHAR.getName(),
                STRING.getName(), LOCALDATETIME.getName(), LOCALDATE.getName(), LOCALTIME.getName())) {
            return "String";
        }
        throw new RuntimeException("出现未考虑到的简单类型");

    }

}
