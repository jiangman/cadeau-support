package com.spldeolin.cadeau.support.doc;

public enum SimpleType {

    BYTE("Byte", "18"), SHORT("Short", "18"), INTEGER("Integer", "18"), LONG("Long", "18"),
    FLOAT("Float", "9.15"), DOUBLE("Double", "9.15"),
    CHARACTER("Character", "L"), BOOLEAN("Boolean", "true"),
    CHAR("Char", "L"), Int("Int", "18"), // 这两个基本数据类型的primitiveTypeName与它们包装类型名不一致
    STRING("String", "曲奇饼干"),
    DATE("Date", "1528627116375"),
    LOCALDATETIME("LocalDateTime", "2018-06-10 18:39:01"), LOCALDATE("LocalDate", "2018-06-10"), LOCALTIME("LocalTime",
            "18:39:01"),
    BIGDECIMAL("BigDecimal", "9.15");

    public enum JsonString {

        CHAR("Char"), STRING("String"), LOCALDATETIME("LocalDateTime"), LOCALDATE("LocalDate"), LOCALTIME(
                "LOCALTIME"), CHARACTER("Character");

        private String name;

        JsonString(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    private String name;

    private String sampleValue;

    SimpleType(String name, String sampleValue) {
        this.name = name;
        this.sampleValue = sampleValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSampleValue() {
        return sampleValue;
    }

    public void setSampleValue(String sampleValue) {
        this.sampleValue = sampleValue;
    }
}


