package com.westwell.server.service.impl;

import com.westwell.api.common.utils.DateUtils;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.enums.TaskStatusEnum;
import com.westwell.server.dao.WcCameraInfoDao;
import com.westwell.server.dao.WcTaskDao;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.entity.WcCameraInfoEntity;
import com.westwell.server.entity.WcTaskEntity;
import com.westwell.server.service.WcTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;


@Service("wcTaskService")
public class WcTaskServiceImpl implements WcTaskService {


    @Resource
    WcTaskDao wcTaskDao;

    @Resource
    WcCameraInfoDao cameraInfoDao;


    @Override
    public WcTaskEntity queryOne() {

//        wcTaskDao.sele
        return wcTaskDao.selectByPrimaryKey(1);
    }


    @Override
    public TaskDetailInfoDto getOneTaskDetailInfoDto() {

        WcTaskEntity taskEntity = WcTaskEntity.builder()
                .cameraNo(DataConfig.CAMERA_NO)
                .taskStatus(TaskStatusEnum.DOING.getCode())
                .taskStartTime(new Date())
                .frame(DataConfig.FRAME.toString())
                .videoStartTime(DateUtils.stringToDate(DataConfig.VIDEO_START_TIME))
                .videoEndTime(DateUtils.stringToDate(DataConfig.VIDEO_END_TIME))
                .build();
        wcTaskDao.insertSelective(taskEntity);

        WcCameraInfoEntity cameraInfoEntity = cameraInfoDao.selectByPrimaryKey(DataConfig.CAMERA_NO);
        TaskDetailInfoDto taskDetailInfoDto = TaskDetailInfoDto.builder()
                .taskEntity(taskEntity)
                .cameraInfoEntity(cameraInfoEntity)
                .build();
        return taskDetailInfoDto;
    }

    @Override
    public TaskDetailInfoDto getOneTaskDetailInfoDto(Integer taskNo, Integer cameraNo, Date videoStartTime, Date videoEndTime) {

        WcTaskEntity taskEntity = WcTaskEntity.builder()
                .taskNo(taskNo)
                .cameraNo(cameraNo)
                .taskStatus(TaskStatusEnum.DOING.getCode())
                .taskStartTime(new Date())
                .frame(DataConfig.FRAME.toString())
                .videoStartTime(videoStartTime)
                .videoEndTime(videoEndTime)
                .build();
        wcTaskDao.insertSelective(taskEntity);

        WcCameraInfoEntity cameraInfoEntity = cameraInfoDao.selectByPrimaryKey(cameraNo);
        TaskDetailInfoDto taskDetailInfoDto = TaskDetailInfoDto.builder()
                .taskEntity(taskEntity)
                .cameraInfoEntity(cameraInfoEntity)
                .build();
        return taskDetailInfoDto;
    }

    @Override
    public void update(WcTaskEntity taskEntity) {
        wcTaskDao.updateByPrimaryKeySelective(taskEntity);
    }
}
