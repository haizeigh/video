package com.westwell.backend.modules.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.westwell.backend.common.utils.PageUtils;
import com.westwell.backend.modules.generator.entity.WcTaskEntity;

import java.util.Map;

/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
public interface WcTaskService extends IService<WcTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

