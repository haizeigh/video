package com.westwell.backend.modules.generator.dao;

import com.westwell.backend.modules.generator.entity.StudentBaseInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生基本信息
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-08 10:15:59
 */
@Mapper
public interface StudentBaseInfoDao extends BaseMapper<StudentBaseInfoEntity> {
	
}
