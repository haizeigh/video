package com.westwell.server.service.impl;

import com.westwell.server.common.enums.TaskStatusEnum;
import com.westwell.server.common.exception.VPException;
import com.westwell.server.container.IdentifyContainer;
import com.westwell.server.container.VideoContainer;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.entity.WcTaskEntity;
import com.westwell.server.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

@Slf4j
@Service
public class VideoProcessServiceImpl implements VideoProcessService {

    @Resource
    WcTaskService wcTaskService;

    @Resource
    VideoMediaService videoMediaService;

    @Resource
    DetectionService detectionService;

    @Resource
    FeatureService featureService;

    @Resource
    IdentifyService identifyService;

    @Resource
    IdentifyContainer identifyContainer;

    @Override
    @Async("taskExecutor")
    public Future<TaskDetailInfoDto> detectVideo(Integer taskNo, Integer cameraNo, Date videoStartTime, Date videoEndTime)  {


        TaskDetailInfoDto task = null;
        try {

//            初始化任务
            task = wcTaskService.getOneTaskDetailInfoDto(taskNo, cameraNo, videoStartTime, videoEndTime);

            //下载视频
            log.info("下载视频...");
            boolean readVideo = videoMediaService.readVideo(task);
            if (!readVideo){
                throw new VPException( task + "任务下载视频错误");
            }

//            String picPath = task.getTaskPath();
            boolean cutVideoToPics = videoMediaService.cutVideoToPics(task);
            if (!cutVideoToPics) {
                throw new VPException("ffmpeg 截图出错");
            }

//           FFMPEG_PATH配置
            log.info("原图保存redis...");
            List<String> picKeyList = videoMediaService.writePicsToRedis(task);

            List<String> faces = identifyFaces(task, picKeyList);

            List<String> bodies = identifyBodies(task, picKeyList);

            log.info("清理原图..");
            videoMediaService.clearListInRedis(picKeyList);

            //todo 处理face 和 body 的关系



        } catch (Exception e) {

            e.printStackTrace();
            if (task != null && task.getTaskEntity() != null){
                UpdateTaskStatus(task, TaskStatusEnum.FAIL);
            }
            throw new VPException("解析视频出现错误", e);
        }

        log.info("解析成功");
        UpdateTaskStatus(task, TaskStatusEnum.SUCCESS);
        return new AsyncResult(task);
    }

    private List<String> identifyBodies(TaskDetailInfoDto task, List<String> picKeyList) throws Exception {

        TaskDetailInfoDto taskDetailInfoDto = new TaskDetailInfoDto();
        BeanUtils.copyProperties(task, taskDetailInfoDto);
        taskDetailInfoDto.setTaskType(TaskDetailInfoDto.TaskType.BODY);
        task = taskDetailInfoDto;

        log.info("人体检测...");
        List<String> bodyKeyList = detectionService.detectBodiesInPic(picKeyList);
        if (CollectionUtils.isEmpty(bodyKeyList)){
            log.info("本次任务没有body");
            return null;
        }

        log.info("输出每一帧的body图");
        videoMediaService.readPicsFromRedis(task, bodyKeyList, task.getTaskTemptPathForBody());

//            保存面部图
        detectionService.storePicFrames(task, bodyKeyList);

//            特征提取
        log.info("body小图特征提取..");
        featureService.extractBodyFeature(bodyKeyList);

//            底库创建以及识别人物
        log.info("开始body识别..");
        identifyService.identifyBody(task, bodyKeyList);
        return bodyKeyList;

    }

    private List<String> identifyFaces(TaskDetailInfoDto task, List<String> picKeyList) throws Exception {
        //            检测人脸
        TaskDetailInfoDto taskDetailInfoDto = new TaskDetailInfoDto();
        BeanUtils.copyProperties(task, taskDetailInfoDto);
        taskDetailInfoDto.setTaskType(TaskDetailInfoDto.TaskType.FACE);
        task = taskDetailInfoDto;


        log.info("人脸检测...");
        List<String> faceKeyList = detectionService.detectFacesInPic(picKeyList);
        if (CollectionUtils.isEmpty(faceKeyList)){
            log.info("本次任务没有face");
            return null;
        }

        log.info("输出每一帧面部");
        videoMediaService.readPicsFromRedis(task, faceKeyList, task.getTaskTemptPathForFace());

        log.info("输出底库");
        videoMediaService.readBasePicsFromRedis(task);
//            保存面部图
        detectionService.storePicFrames(task, faceKeyList);


//            特征提取
        log.info("小图特征提取..");
        featureService.extractFaceFeature(faceKeyList);

//            底库创建以及识别人物
        log.info("开始人物识别..");
        identifyService.identifyFaces(task, faceKeyList);

        return faceKeyList;
    }

    public void clearVideoCache(TaskDetailInfoDto task) {
        //todo 清理任务
        log.info("格式化单次任务");
//        本地图片 redis原图 redis小图 redis小图集合 本地帧集合 本地容器
        VideoContainer videoContainer = identifyContainer.getIdentifyMap().get(task.getTaskCameraPrefix());
        if (videoContainer == null){
            return;
        }


        List<String> sortedFaceKeys = identifyContainer.getSortedFaceKeys(task);
        videoMediaService.clearListInRedis(sortedFaceKeys);

        List<String> list = identifyContainer.faceColleKeys(task);
        videoMediaService.clearListInRedis(list);
        identifyContainer.getIdentifyMap().remove(task.getTaskCameraPrefix());

    }


    private void UpdateTaskStatus(TaskDetailInfoDto task, TaskStatusEnum taskStatusEnum) {
        WcTaskEntity taskEntity = task.getTaskEntity();
        taskEntity.setTaskStatus(taskStatusEnum.getCode());
        taskEntity.setTaskEndTime(new Date());
        wcTaskService.update(taskEntity);
    }
}
