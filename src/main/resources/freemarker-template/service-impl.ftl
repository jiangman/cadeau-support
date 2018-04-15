package ${basePackage}.service.impl${bussinessPart};

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ${derivedServiceImplRef};
import ${basePackage}.dao${bussinessPart}.${modelName}Mapper;
import ${basePackage}.model${bussinessPart}.${modelName};
import ${basePackage}.service${bussinessPart}.${modelName}Service;
import lombok.extern.log4j.Log4j2;
import tk.mybatis.mapper.entity.Condition;
import com.spldeolin.cadeau.library.exception.ServiceException;
import com.spldeolin.cadeau.library.util.FieldExtractUtil;
import com.spldeolin.cadeau.library.dto.Page;
import com.github.pagehelper.PageHelper;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example;

/**
 * “${modelCn}”业务实现
${classDocEnd}
@Service
@Log4j2
public class ${modelName}ServiceImpl extends ${derivedServiceImplName}<${modelName}> implements ${modelName}Service {

    @Autowired
    private ${modelName}Mapper ${modelName?uncap_first}Mapper;

    @Override
    public Long createEX(${modelName} ${modelName?uncap_first}) {
        /* 业务校验 */
        super.create(${modelName?uncap_first});
        return ${modelName?uncap_first}.getId();
    }

    @Override
    public void updateEX(${modelName} ${modelName?uncap_first}) {
        if (!isExist(${modelName?uncap_first}.getId())) {
            throw new ServiceException("${modelCn}不存在或是已被删除");
        }
        /* 业务校验 */
        super.update(${modelName?uncap_first});
    }

    @Override
    public void deleteEX(Long id) {
        if (!isExist(id)) {
            throw new ServiceException("${modelCn}不存在或是已被删除");
        }
        /* 业务校验 */
        super.delete(id);
    }

    @Override
    public String deleteEX(List<Long> ids) {
        List<${modelName}> exist = get(ids);
        if (exist.size() == 0) {
            throw new ServiceException("选中的${modelCn}全部不存在或是已被删除");
        }
        /* 业务校验 */
        super.delete(FieldExtractUtil.extractId(exist));
        return "操作成功";
    }

    @Override
    public Page<${modelName}> page(Integer pageNo, Integer pageSize) {
        Condition condition = new Condition(User.class);
        condition.createCriteria()/* 添加条件 */;
        PageHelper.startPage(pageNo, pageSize);
        return Page.wrap(userMapper.selectBatchByCondition(condition));
    }

}