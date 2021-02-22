package com.westwell.backend.modules.generator.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.westwell.backend.common.utils.PageUtils;
import com.westwell.backend.common.utils.Query;

import com.westwell.backend.modules.generator.dao.WcTaskDao;
import com.westwell.backend.modules.generator.entity.WcTaskEntity;
import com.westwell.backend.modules.generator.service.WcTaskService;


@Service("wcTaskService")
public class WcTaskServiceImpl extends ServiceImpl<WcTaskDao, WcTaskEntity> implements WcTaskService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WcTaskEntity> page = this.page(
                new Query<WcTaskEntity>().getPage(params),
                new QueryWrapper<WcTaskEntity>()
        );

        return new PageUtils(page);
    }

}
