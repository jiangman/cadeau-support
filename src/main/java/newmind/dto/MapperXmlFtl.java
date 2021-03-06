package newmind.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Deolin 2018/11/14
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class MapperXmlFtl implements Serializable {

    private String packageReference;

    private String modelName;

    private static final long serialVersionUID = 1L;

}