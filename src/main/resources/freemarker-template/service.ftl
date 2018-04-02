package ${basePackage}.service${bussinessPart};

import java.util.List;
import ${basePackage}.model${bussinessPart}.${modelName};
import ${derivedServiceRef};
import com.spldeolin.cadeau.library.dto.Page;

/**
 * “${modelCn}”业务
${classDocEnd}
public interface ${modelName}Service extends ${derivedServiceName}<${modelName}> {

    /**
     * 创建一个“${modelCn}”
     * （附带业务校验）
     *
     * @param user 待创建“${modelCn}”
     * @return 自增ID
     */
    Long createEX(${modelName} ${modelName?uncap_first});

    /**
     * 更新一个“${modelCn}”
     * （附带业务校验）
     *
     * @param user 待更新“${modelCn}”
     */
    void updateEX(${modelName} ${modelName?uncap_first});

    /**
     * 删除一个“${modelCn}”
     * 
     *
     * @param id 待删除“${modelCn}”的ID
     */
    void deleteEX(Long id);

    /**
     * 删除多个资源
     （附带业务校验，并返回详细情况）
     *
     * @param ids 待删除资源的ID列表
     * @return 删除情况
     */
    String deleteEX(List<Long> ids);

    /**
     * 分页获取资源
     *
     * @param pageNo 页码
     * @param pageSize 分页尺寸
     * @return Page 分页对象
     */
    Page<${modelName}> page(Integer pageNo, Integer pageSize); // 根据具体需求拓展这个方法（追加搜索用参数等）

	// 其他方法声明

}