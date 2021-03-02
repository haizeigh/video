package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.entity.WcTaskEntity;

import java.util.Date;

/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
public interface WcTaskService {

    WcTaskEntity queryOne();

//    创建任务
    TaskDetailInfoDto getOneTaskDetailInfoDto();

    TaskDetailInfoDto getOneTaskDetailInfoDto(Integer taskNo, Integer cameraNo, Date videoStartTime, Date videoEndTime);

    void update(WcTaskEntity taskEntity);
}

