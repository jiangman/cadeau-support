package ${packageReference}.model;

import java.io.Serializable;
import java.time.*;
import com.baomidou.mybatisplus.annotation.*;
import ${packageReference}.api.IdGetable;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * @author ${author}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@TableName("${tableName}")
public class ${modelName} implements IdGetable, Serializable {

<#list properties as property>
    @TableField("${property.columnName}")
    private ${property.fieldType} ${property.fieldName};
</#list>

    private static final long serialVersionUID = 1L;

}
