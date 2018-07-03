/*
 * Generated by Cadeau Support.
 *
 * https://github.com/spldeolin/cadeau-support
 */

package ${basePackage}.controller${bussinessPart};

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ${basePackage}.model${bussinessPart}.${modelName};
import ${basePackage}.input${bussinessPart}.${modelName}Input;
import ${basePackage}.service${bussinessPart}.${modelName}Service;
import ${pageRef};
import ${pageParamRef};

/**
 * “${modelCn}”管理
${classDocEnd}
@RestController
@RequestMapping("/${modelName ?uncap_first}")
@Validated
public class ${modelName}Controller {

    @Autowired
    private ${modelName}Service ${modelName ?uncap_first}Service;

    /**
     * 创建一个“${modelCn}”
     *
     * @param ${modelName ?uncap_first}Input 待创建的“${modelCn}”
     * @return 创建成功后生成的ID
     */
    @PostMapping("/create")
    Long create(@RequestBody @Valid ${modelName}Input ${modelName ?uncap_first}Input) {
        return ${modelName ?uncap_first}Service.createEX(${modelName ?uncap_first}Input.toModel());
    }

    /**
     * 获取一个“${modelCn}”
     *
     * @param id 待获取“${modelCn}”的ID
     * @return ${modelCn}
     */
    @GetMapping("/get")
    ${modelName} get(@RequestParam Long id) {
        return ${modelName ?uncap_first}Service.getEX(id);
    }

    /**
     * 更新一个“${modelCn}”
     *
     * @param id 待更新“${modelCn}”的ID
     * @param ${modelName ?uncap_first}Input 待更新的“${modelCn}”
     */
    @PostMapping("/update")
    void update(@RequestParam Long id, @RequestBody @Valid ${modelName}Input ${modelName ?uncap_first}Input) {
        ${modelName ?uncap_first}Service.updateEX(${modelName ?uncap_first}Input.toModel().setId(id));
    }

    /**
     * 删除一个“${modelCn}”
     *
     * @param id 待删除“${modelCn}”的ID
     */
    @PostMapping("/delete")
    void delete(@RequestParam Long id) {
        ${modelName ?uncap_first}Service.deleteEX(id);
    }

    /**
     * 获取一批“${modelCn}”
     *
     * @param pageParam 页码和每页条目数
     * @return “${modelCn}”分页
     */
    @GetMapping("/search")
    Page<${modelName}> search(PageParam pageParam) {
        return ${modelName ?uncap_first}Service.page(pageParam);
    }

    /**
     * 删除一批“${modelCn}”
     *
     * @param ids 待删除“${modelCn}”的ID列表
     * @return 删除情况
     */
    @PostMapping("/batchDelete")
    String delete(@RequestParam List<Long> ids) {
        return ${modelName ?uncap_first}Service.deleteEX(ids);
    }

}