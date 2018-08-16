package com.spldeolin.cadeau.support.docex;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/08/16
 */
@Log4j2
@Data
public class SimpleType {

    public static final SimpleType STRING;

    public static final SimpleType DATE;

    public static final SimpleType TIME;

    public static final SimpleType DATA_TIME;

    public static final SimpleType INTEGER;

    public static final SimpleType FRACTION;

    public static final SimpleType TRUE_FALSE;

    public static final SimpleType NULL;

    private static final List<SimpleType> ALL_SIMPLE_TYPES;

    private String jsonTypeName;

    private List<Class> javaTypes;

    private String exampleValue;

    static {
        STRING = new SimpleType();
        STRING.setJsonTypeName("string");
        STRING.setJavaTypes(Lists.newArrayList(String.class));
        STRING.setExampleValue("\"曲奇饼干\"");

        DATE = new SimpleType();
        DATE.setJsonTypeName("string");
        DATE.setJavaTypes(Lists.newArrayList(LocalDate.class));
        DATE.setExampleValue("2018-08-16");

        TIME = new SimpleType();
        TIME.setJsonTypeName("string");
        TIME.setJavaTypes(Lists.newArrayList(LocalTime.class));
        TIME.setExampleValue("11:17:59");

        DATA_TIME = new SimpleType();
        DATA_TIME.setJsonTypeName("string");
        DATA_TIME.setJavaTypes(Lists.newArrayList(LocalDateTime.class));
        DATA_TIME.setExampleValue("2018-08-16T11:17:59");

        INTEGER = new SimpleType();
        INTEGER.setJsonTypeName("integer");
        List<Class> integerJavaTypes = Lists.newArrayList();
        integerJavaTypes.add(Byte.class);
        integerJavaTypes.add(Short.class);
        integerJavaTypes.add(Integer.class);
        integerJavaTypes.add(Long.class); // 可追加更多
        INTEGER.setJavaTypes(integerJavaTypes);
        INTEGER.setExampleValue("65535");

        FRACTION = new SimpleType();
        FRACTION.setJsonTypeName("fraction");
        List<Class> fractionJavaTypes = Lists.newArrayList();
        fractionJavaTypes.add(Float.class);
        fractionJavaTypes.add(Double.class);
        fractionJavaTypes.add(BigDecimal.class); // 可追加更多
        FRACTION.setJavaTypes(fractionJavaTypes);
        FRACTION.setExampleValue("9.15");

        TRUE_FALSE = new SimpleType();
        TRUE_FALSE.setJsonTypeName("true/false");
        TRUE_FALSE.setJavaTypes(Lists.newArrayList(Boolean.class));
        TRUE_FALSE.setExampleValue("true");

        NULL = new SimpleType();
        NULL.setJsonTypeName("null");
        NULL.setExampleValue("null");

        ALL_SIMPLE_TYPES = Lists.newArrayList();
        for (Field field : SimpleType.class.getDeclaredFields()) {
            if (field.getType() == SimpleType.class) {
                try {
                    ALL_SIMPLE_TYPES.add((SimpleType) field.get(SimpleType.class));
                } catch (IllegalAccessException e) {
                    log.error(e);
                }
            }
        }
    }

    public static boolean isSimpleType(Class javaType) {
        for (SimpleType simpleType : ALL_SIMPLE_TYPES) {
            if (simpleType.getJavaTypes().contains(javaType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是简单类型
     */
    public static String getJsonTypeName(Class javaType) {
        for (SimpleType simpleType : ALL_SIMPLE_TYPES) {
            if (simpleType.getJavaTypes().contains(javaType)) {
                return simpleType.getJsonTypeName();
            }
        }
        throw new RuntimeException(javaType.getSimpleName() + "不是简单类型，调用此方法前需要调用isSimpleType进行保护");
    }

    public static String getExampleValue(Class javaType) {
        for (SimpleType simpleType : ALL_SIMPLE_TYPES) {
            if (simpleType.getJavaTypes().contains(javaType)) {
                return simpleType.getExampleValue();
            }
        }
        throw new RuntimeException(javaType.getSimpleName() + "不是简单类型，调用此方法前需要调用isSimpleType进行保护");
    }

}
