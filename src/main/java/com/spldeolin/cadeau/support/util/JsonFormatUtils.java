package com.spldeolin.cadeau.support.util;

import lombok.extern.log4j.Log4j2;

/**
 * @author Deolin 2018/06/10
 */
@Log4j2
public class JsonFormatUtils {

    /**
     * 格式化
     */
    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr))
            return "";
        StringBuilder sb = new StringBuilder();
        char last;
        char current = '\0';
        int indent = 0;
        boolean isInQuotationMarks = false;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                /*
                    "a":null
                    "a" : null
                 */
                case ':':
                    if (jsonStr.charAt(i - 1) == '"') {
                        sb.append(current);
                        sb.append(' ');
                        break;
                    }
                case '"':
                    if (last != '\\') {
                        isInQuotationMarks = !isInQuotationMarks;
                    }
                    sb.append(current);
                    break;
                case '{':
                case '[':
                    sb.append(current);
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent++;
                        addIndentBlank(sb, indent);
                    }
                    break;
                case '}':
                case ']':
                    if (!isInQuotationMarks) {
                        sb.append('\n');
                        indent--;
                        addIndentBlank(sb, indent);
                    }
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\' && !isInQuotationMarks) {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString();
    }

    /**
     * 修剪掉非标准JSON中多余的逗号
     */
    public static String trim(String invalidJson) {
        // 修剪掉多余的逗号
        String validJson = invalidJson.replace(",]", "]").replace(",}", "}");
        return validJson;
    }

    /**
     * 添加缩进符
     */
    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("    ");
        }
    }

}
