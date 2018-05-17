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
import ${serviceExceptionRef};
import ${requestResult};
import ${basePackage}.input${bussinessPart}.${modelName}Input;
import ${basePackage}.service${bussinessPart}.${modelName}Service;

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
     */
    @PostMapping
    public RequestResult create(@RequestBody @Valid ${modelName}Input ${modelName ?uncap_first}Input) {
        return RequestResult.success(${modelName ?uncap_first}Service.createEX(${modelName ?uncap_first}Input.toModel()));
    }

    /**
     * 获取一个“${modelCn}”
     */
    @GetMapping("/{id}")
    public RequestResult get(@PathVariable Long id) {
        return RequestResult.success(${modelName ?uncap_first}Service.get(id).orElseThrow(() -> new ServiceException("${modelCn}不存在或是已被删除")));
    }

    /**
     * 更新一个“${modelCn}”
     */
    @PutMapping("/{id}")
    public RequestResult update(@PathVariable Long id, @RequestBody @Valid ${modelName}Input ${modelName ?uncap_first}Input) {
${modelName ?uncap_first}Service.updateEX(${modelName ?uncap_first}Input.toModel().setId(id));
        return RequestResult.success();
    }

    /**
     * 删除一个“${modelCn}”
     */
    @DeleteMapping("/{id}")
    public RequestResult delete(@PathVariable Long id) {
${modelName ?uncap_first}Service.deleteEX(id);
        return RequestResult.success();
    }

    /**
     * 获取一批“${modelCn}”
     */
    @GetMapping
    public RequestResult page(@RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") @Max(1000) Integer pageSize) {
        return RequestResult.success(${modelName ?uncap_first}Service.page(pageNo, pageSize));
    }

    /**
     * 删除一批“${modelCn}”
     */
    @PutMapping("/batchDelete")
    public RequestResult delete(@RequestBody List<Long> ids) {
        return RequestResult.success(${modelName ?uncap_first}Service.deleteEX(ids));
    }

}