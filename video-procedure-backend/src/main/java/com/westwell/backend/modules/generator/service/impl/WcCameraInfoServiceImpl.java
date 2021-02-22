package com.westwell.backend.modules.generator.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.westwell.backend.common.utils.PageUtils;
import com.westwell.backend.common.utils.Query;

import com.westwell.backend.modules.generator.dao.WcCameraInfoDao;
import com.westwell.backend.modules.generator.entity.WcCameraInfoEntity;
import com.westwell.backend.modules.generator.service.WcCameraInfoService;


@Service("wcCameraInfoService")
public class WcCameraInfoServiceImpl extends ServiceImpl<WcCameraInfoDao, WcCameraInfoEntity> implements WcCameraInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WcCameraInfoEntity> page = this.page(
                new Query<WcCameraInfoEntity>().getPage(params),
                new QueryWrapper<WcCameraInfoEntity>()
        );

        return new PageUtils(page);
    }

}
