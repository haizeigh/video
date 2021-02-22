package com.westwell.backend.modules.generator.dao;

import com.westwell.backend.modules.generator.entity.WcTaskEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
@Mapper
public interface WcTaskDao extends BaseMapper<WcTaskEntity> {

}
