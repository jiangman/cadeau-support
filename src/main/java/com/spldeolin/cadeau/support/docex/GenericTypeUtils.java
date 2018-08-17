package com.spldeolin.cadeau.support.docex;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * 泛型工具
 *
 * 用于获取方法的返回值、方法的参数、类的字段的泛型类型
 *
 * @author Deolin 2018/08/17
 */
@Log4j2
public class GenericTypeUtils {

    /**
     * 获取【方法返回值】类型的泛型类型
     */
    public static Class getGenericReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            if (actualTypeArguments.length > 1) {
                throw new RuntimeException("方法" + method.getName() + "的返回值类型中的泛型不只一个");
            }
            return (Class) actualTypeArguments[0];
        }
        throw new RuntimeException("方法" + method.getName() + "的返回值类型中没有泛型");
    }

    /**
     * 获取【类字段】类型的泛型类型
     */
    public static Class getGenericFieldType(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (actualTypeArguments.length > 1) {
                throw new RuntimeException("字段" + field.getName() + "类型中的泛型不只一个");
            }
            return (Class) actualTypeArguments[0];
        }
        throw new RuntimeException("字段" + field.getName() + "类型中没有泛型");
    }

    /**
     * 获取【方法的参数】类型的泛型类型
     */
    public static Class getGenericParameterType(Parameter parameter) {
        Type genericType = parameter.getParameterizedType();
        if (genericType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (actualTypeArguments.length > 1) {
                throw new RuntimeException("参数" + parameter.getName() + "类型中的泛型不只一个");
            }
            return (Class) actualTypeArguments[0];
        }
        throw new RuntimeException("参数" + parameter + "类型中没有泛型");
    }

    @SneakyThrows
    public static void main(String[] args) {
        Method declaredMethod = GenericTypeUtils.class.getDeclaredMethod("a", List.class);
        log.info(getGenericReturnType(declaredMethod));

        Field declaredField = GenericTypeUtils.class.getDeclaredField("b");
        log.info(getGenericFieldType(declaredField));

        Parameter parameter = declaredMethod.getParameters()[0];
        log.info(getGenericParameterType(parameter));
    }

    private List<String> a(List<Double> c) {
        return null;
    }

    private List<Integer> b;

}
