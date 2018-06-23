package com.spldeolin.cadeau.support.doc;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MarkdownDocFTL {

    /**
     * 目录名
     * （本字段不用于渲染FreeMarker）
     */
    private String directoryName;

    /**
     * 文件名
     * （本字段不用于渲染FreeMarker）
     */
    private String fileName;

    /**
     * 描述
     */
    private String commonDesc;

    /**
     * 请求URL
     */
    private String httpUrl;

    /**
     * 请求动词
     */
    private String httpMethod;

    /**
     * 是否显示“参数说明”
     */
    private Boolean paramShow;

    /**
     * 参数说明
     */
    private List<PField> paramFields;

    /**
     * 是否显示“请求体示例”和“请求体说明”
     */
    private Boolean bodyShow;

    /**
     * 请求体示例
     */
    private String bodyJson;

    /**
     * 返回值是否是简单类型，是的话则不显示“返回值说明”
     */
    private Boolean isBodySimpleType;

    /**
     * 是否显示“请求体描述”和“请求体具体字段说明”
     * （如果请求体是个简单类型 且 请求体描述为空 则false）
     */
    private Boolean displayBodyInfo;

    /**
     * 请求体描述
     */
    private String bodyDesc;

    /**
     * 请求体具体字段说明
     */
    private List<BField> bodyFields;

    /**
     * 是否显示“返回值示例”和“返回值说明”
     */
    private Boolean returnShow;

    /**
     * 返回值示例
     */
    private String returnJson;

    /**
     * 返回值是否是简单类型，是的话则不显示“返回值说明”
     */
    private Boolean isRetrunSimpleType;

    /**
     * 是否显示“返回值描述”和“返回值具体字段说明”
     * （如果返回值是个简单类型 且 返回值描述为空 则false）
     */
    private Boolean displayReturnInfo;

    /**
     * 返回值描述
     */
    private String returnDesc;

    /**
     * 返回值具体字段说明
     */
    private List<RField> returnFields;

    /**
     * 开发者
     */
    private String commonDeveloper;

    @Data
    public static class PField {

        private String paramName;

        /**
         * query, body, path
         */
        private String paramPlace;

        private String paramRequired;

        private String paramType;

        private String paramDesc;

    }

    @Data
    public static class BField {

        private String bodyName;

        private String bodyRequired;

        private String bodyType;

        private String bodyDesc;
    }

    @Data
    public static class RField {

        private String returnName;

        private String returnType;

        private String returnDesc;

    }

}
