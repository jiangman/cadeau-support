package com.spldeolin.cadeau.support.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Custom {

    // api的全限定名
    public static String service = "";

    private static String serviceImpl = "";

    private static String mapper = "";

    // 注释类

    public static String date = new SimpleDateFormat("yyyy/M/d").format(new Date());

    public static String author = "Deolin";

    public static String generatorTag = " * @generator Cadeau Support";

    // 业务接口

    public static String basePackage = "com.spldeolin.cadeau.demo";




/*
    数据库ip、端口、用户名、密码、库名
    是否覆盖
    项目路径、基础包名
    页面模块
    表名
    Service全限定名
    ServiceImpl全限定名
    Mapper全限定名
*/

/* TODO package
    {basePackage}.dao.{bussiness}
    mapper.{bussiness}
    {basePackage}.model.{bussiness}
    {basePackage}.input.{bussiness}
    {basePackage}.service.{bussiness}
    {basePackage}.service.impl.{bussiness}
 */

/* TODO output path
    projectPath + mavenJava + package.replace('.', sep) + sep;
 */

/* TODO generators
    PersistenceAndDtoGenerator
    ServiceGenerator
    ControllerGenerator
 */

}
