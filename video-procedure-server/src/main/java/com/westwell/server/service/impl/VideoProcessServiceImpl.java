package com.westwell.server.service.impl;

import com.westwell.server.common.enums.TaskStatusEnum;
import com.westwell.server.common.exception.VPException;
import com.westwell.server.container.IdentifyFacesContainer;
import com.westwell.server.container.VideoContainer;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.entity.WcTaskEntity;
import com.westwell.server.service.*;
import lombok.extern.slf4j.Slf4j;
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
    FaceDetectionService faceDetectionService;

    @Resource
    FaceFeatureService faceFeatureService;

    @Resource
    FaceIdentifyService faceIdentifyService;

    @Resource
    IdentifyFacesContainer identifyFacesContainer;

    @Override
    @Async("taskExecutor")
    public Future<TaskDetailInfoDto> detectVideo(Integer cameraNo, Date videoStartTime, Date videoEndTime)  {


        TaskDetailInfoDto task = null;
        try {

//            初始化任务
            task = wcTaskService.getOneTaskDetailInfoDto(cameraNo, videoStartTime, videoEndTime);

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

//            检测人脸
            log.info("人脸检测...");
            List<String> faceKeyList = faceDetectionService.detectFacesInPic(picKeyList);

            log.info("输出每一帧面部");
            videoMediaService.readPicsFromRedis(task, faceKeyList);

            log.info("输出底库");
            videoMediaService.readBasePicsFromRedis(task);
//            保存面部图
            faceDetectionService.storeFaces(task, faceKeyList);
            log.info("清理原图..");
            videoMediaService.clearListInRedis(picKeyList);

//            特征提取
            log.info("小图特征提取..");
            faceFeatureService.extractFaceFeature(faceKeyList);

//            底库创建以及识别人物
            log.info("开始人物识别..");
            faceIdentifyService.identifyFaces(task, faceKeyList);

            log.info("输出临时底库无标签的图片");
            videoMediaService.readPicCollesFromRedis(task);

            log.info("输出临时底库有标签的图片");
            videoMediaService.readfacesCollesFromRedis(task);

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

    public void clearVideoCache(TaskDetailInfoDto task) {
        log.info("格式化单次任务");
//        本地图片 redis原图 redis小图 redis小图集合 本地帧集合 本地容器
        VideoContainer videoContainer = identifyFacesContainer.getIdentifyMap().get(task.getTaskCameraPrefix());
        if (videoContainer == null){
            return;
        }


        List<String> sortedFaceKeys = identifyFacesContainer.getSortedFaceKeys(task);
        videoMediaService.clearListInRedis(sortedFaceKeys);

        List<String> list = identifyFacesContainer.faceColleKeys(task);
        videoMediaService.clearListInRedis(list);
        identifyFacesContainer.getIdentifyMap().remove(task.getTaskCameraPrefix());

    }


    private void UpdateTaskStatus(TaskDetailInfoDto task, TaskStatusEnum taskStatusEnum) {
        WcTaskEntity taskEntity = task.getTaskEntity();
        taskEntity.setTaskStatus(taskStatusEnum.getCode());
        taskEntity.setTaskEndTime(new Date());
        wcTaskService.update(taskEntity);
    }
}
