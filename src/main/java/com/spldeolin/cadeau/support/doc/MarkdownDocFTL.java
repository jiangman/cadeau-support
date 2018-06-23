package com.spldeolin.cadeau.support.doc;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MarkdownDocFTL {

    /**
     * 上传到ShowDoc的目录名，本字段不用于渲染FreeMarker，通过ControllerParser分析得出
     */
    private String directoryName;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * “描述”，也会作为文件名，通过ControllerParser分析得出
     */
    private String commonDesc;

    /**
     * 请求URL，通过ControllerParser分析得出
     */
    private String httpUrl;

    /**
     * 请求动词，通过ControllerParser分析得出
     */
    private String httpMethod;

    /**
     * 是否显示“参数说明”和“请求体示例”，通过ControllerParser分析得出
     */
    private Boolean paramShow;

    /**
     * 参数说明列表，通过ExplainEntryGenerator生成
     */
    private List<PField> paramFields;

    /**
     * 是否显示“请求体示例”
     */
    private Boolean paramBodyShow;

    /**
     * 请求体示例，通过SampleJsonGenerator生成
     */
    private String paramBodyJson;

    private List<BField> paramBodyFields;

    /**
     * 是否显示“返回值示例”和“返回值说明”，通过ControllerParser分析得出
     */
    private Boolean returnShow;

    /**
     * 返回值示例，通过SampleJsonGenerator生成
     */
    private String returnJson;

    /**
     * 返回值是否是简单类型，如果是的话则不显示返回值说明，ExplainEntryGenerator分析
     */
    private Boolean isRetrunSimpleType;

    /**
     * 返回值说明列表，通过ExplainEntryGenerator生成
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
