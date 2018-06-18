package com.spldeolin.cadeau.support.doc.helper;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import com.spldeolin.cadeau.support.doc.SimpleType;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.PrimitiveType;
import japa.parser.ast.type.ReferenceType;
import japa.parser.ast.type.Type;
import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/17
 */
@Log4j2
public class TypeHelper {

    public static boolean isSimpleType(Type type) {
        return isSimpleType(getTypeName(type));
    }

    public static boolean isSimpleType(String typeName) {
        List<SimpleType> simpleTypes = newArrayList(SimpleType.values());
        List<String> simpleTypeNames = simpleTypes.stream().map(SimpleType::getName).collect(Collectors.toList());
        return typeJudgement(typeName, simpleTypeNames.toArray(new String[] {}));
    }

    public static boolean isListOrSet(Type type) {
        return typeJudgement(getTypeName(type), "List", "Set");
    }

    public static boolean isPage(Type type) {
        return typeJudgement(getTypeName(type), "Page");
    }

    public static boolean typeJudgement(String typeName, String... typeNames) {
        return StringUtils.equalsAny(typeName, typeNames);
    }

    public static String getTypeName(Type type) {
        if (type instanceof ReferenceType) {
            ReferenceType fieldTypeEx = (ReferenceType) type;
            Type fieldTypeExType = fieldTypeEx.getType();
            if (fieldTypeExType instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType fieldTypeExTypeEx = (ClassOrInterfaceType) fieldTypeExType;
                return fieldTypeExTypeEx.getName();
            }
        }
        // 泛型会进入这个分支
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType fieldTypeExTypeEx = (ClassOrInterfaceType) type;
            return fieldTypeExTypeEx.getName();
        }
        // 基本数据类型会进入这个分支
        if (type instanceof PrimitiveType) {
            PrimitiveType primitiveType = (PrimitiveType) type;
            return primitiveType.getType().name();
        }
        return "";
    }

    public static Type getGenericType(Type type) {
        if (type instanceof ReferenceType) {
            ReferenceType fieldTypeEx = (ReferenceType) type;
            Type fieldTypeExType = fieldTypeEx.getType();
            if (fieldTypeExType instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType fieldTypeExTypeEx = (ClassOrInterfaceType) fieldTypeExType;
                List<Type> args = fieldTypeExTypeEx.getTypeArgs();
                if (args != null && args.size() > 0) {
                    Type arg = args.get(0);
                    if (arg instanceof ReferenceType) {
                        ReferenceType argEx = (ReferenceType) arg;
                        Type argExType = argEx.getType();
                        return argExType;
                    }
                }
            }
        }
        return type;
    }

    public static String sampleValueBySimpleType(Type type) {
        String actualTypeName = TypeHelper.getTypeName(type);
        return sampleValueBySimpleType(actualTypeName);
    }

    public static String sampleValueBySimpleType(String typeName) {
        String sampleValue = "null";
        for (SimpleType simpleType : SimpleType.values()) {
            if (simpleType.getName().equals(typeName)) {
                sampleValue = simpleType.getSampleValue();
                for (SimpleType.JsonString jsonString : SimpleType.JsonString.values()) {
                    if (jsonString.getName().equals(typeName)) {
                        sampleValue = wrapDoubleQuotes(sampleValue);
                        break;
                    }
                }
                break;
            }
        }
        return sampleValue;
    }

    public static String wrapDoubleQuotes(String string) {
        return "\"" + string + "\"";
    }

}
