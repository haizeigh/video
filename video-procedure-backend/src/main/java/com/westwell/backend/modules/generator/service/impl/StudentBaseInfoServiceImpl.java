package com.westwell.backend.modules.generator.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.westwell.backend.common.utils.PageUtils;
import com.westwell.backend.common.utils.Query;

import com.westwell.backend.modules.generator.dao.StudentBaseInfoDao;
import com.westwell.backend.modules.generator.entity.StudentBaseInfoEntity;
import com.westwell.backend.modules.generator.service.StudentBaseInfoService;


@Service("studentBaseInfoService")
public class StudentBaseInfoServiceImpl extends ServiceImpl<StudentBaseInfoDao, StudentBaseInfoEntity> implements StudentBaseInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<StudentBaseInfoEntity> page = this.page(
                new Query<StudentBaseInfoEntity>().getPage(params),
                new QueryWrapper<StudentBaseInfoEntity>()
        );

        return new PageUtils(page);
    }

}