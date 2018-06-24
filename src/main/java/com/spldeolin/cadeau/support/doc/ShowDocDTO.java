/*
 * Created by IntelliJ IDEA File Templates.
 */

package com.spldeolin.cadeau.support.doc;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2018/06/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class ShowDocDTO implements Serializable {

    private String api_key;

    private String api_token;

    private String cat_name;

    private String cat_name_sub;

    private String page_title;

    private String page_content;

    private Integer s_number;

    private static final long serialVersionUID = 1L;

}