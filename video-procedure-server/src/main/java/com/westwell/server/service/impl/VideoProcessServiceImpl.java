package com.westwell.server.service.impl;

import com.westwell.server.common.enums.TaskStatusEnum;
import com.westwell.server.common.exception.VPException;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainerManager;
import com.westwell.server.container.VideoContainer;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.entity.WcTaskEntity;
import com.westwell.server.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
    IdentifyContainerManager identifyContainerManager;


    @Resource
    RedisUtils redisUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    @Async("taskExecutor")
    public Future<TaskDetailInfoDto> detectVideo(Integer taskNo, Integer cameraNo, Date videoStartTime, Date videoEndTime)  {


        TaskDetailInfoDto task = null;
        List<String> picKeyList = new ArrayList<>();
        try {

            // todo 测试
//            Set<String> keys = redisTemplate.keys("*");
//            keys.stream().forEach(key -> redisUtils.delete(key));
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
            picKeyList = videoMediaService.writePicsToRedis(task);

            List<String> faces = identifyFaces(task, picKeyList);
            //body的识别依赖于face，不方便改为异步
//            List<String> bodies = identifyBodies(task, picKeyList);

            //todo 处理face 和 body 的关系



        } catch (Exception e) {

            e.printStackTrace();
            if (task != null && task.getTaskEntity() != null){
                UpdateTaskStatus(task, TaskStatusEnum.FAIL);
            }
            log.info("解析失败");
            return new AsyncResult(task);
//            throw new VPException("解析视频出现错误", e);
        }finally {
            log.info("清理原图..");
            videoMediaService.clearListInRedis(picKeyList);
        }

        log.info("解析成功");
        UpdateTaskStatus(task, TaskStatusEnum.SUCCESS);
        return new AsyncResult(task);
    }

    private List<String> identifyBodies(TaskDetailInfoDto task, List<String> picKeyList) throws Exception {



        log.info("人体检测...");
        List<String> bodyKeyList = detectionService.detectBodiesInPic(picKeyList);
        if (CollectionUtils.isEmpty(bodyKeyList)){
            log.info("本次任务没有body");
            return null;
        }

        task.setBodies(bodyKeyList);
        TaskDetailInfoDto taskDetailInfoDto = new TaskDetailInfoDto();
        BeanUtils.copyProperties(task, taskDetailInfoDto);
        taskDetailInfoDto.setTaskType(TaskDetailInfoDto.TaskType.BODY);
        task = taskDetailInfoDto;

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

        log.info("人脸检测...");
        List<String> faceKeyList = detectionService.detectFacesInPic(picKeyList);
        if (CollectionUtils.isEmpty(faceKeyList)){
            log.info("本次任务没有face");
            return null;
        }

        task.setFaces(faceKeyList);
        TaskDetailInfoDto taskDetailInfoDto = new TaskDetailInfoDto();
        BeanUtils.copyProperties( task, taskDetailInfoDto, "taskCameraPrefix");
        taskDetailInfoDto.setTaskType(TaskDetailInfoDto.TaskType.FACE);
        task = taskDetailInfoDto;

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
        log.info("格式化单次任务no={}", task.getTaskEntity().getTaskNo());
//        本地图片 redis原图 redis小图 redis小图集合 本地帧集合 本地容器
        VideoContainer videoContainer = identifyContainerManager.getVideoContainerMap().get(task.getTaskCameraPrefix());
        if (videoContainer == null){
            return;
        }


        List<String> sortedFaceKeys = identifyContainerManager.getSortedFaceKeys(task);
        videoMediaService.clearListInRedis(sortedFaceKeys);

        List<String> list = identifyContainerManager.picColleKeys(task);
        videoMediaService.clearListInRedis(list);
        identifyContainerManager.getVideoContainerMap().remove(task.getTaskCameraPrefix());

    }


    private void UpdateTaskStatus(TaskDetailInfoDto task, TaskStatusEnum taskStatusEnum) {
        WcTaskEntity taskEntity = task.getTaskEntity();
        taskEntity.setTaskStatus(taskStatusEnum.getCode());
        taskEntity.setTaskEndTime(new Date());
        wcTaskService.update(taskEntity);
    }

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {

        TaskDetailInfoDto taskDetailInfoDto1 = new TaskDetailInfoDto();
        taskDetailInfoDto1.setTaskPath("test");

        TaskDetailInfoDto taskDetailInfoDto2 = new TaskDetailInfoDto();

        org.apache.commons.beanutils.BeanUtils.copyProperties(taskDetailInfoDto1, taskDetailInfoDto2);
        System.out.println(taskDetailInfoDto2);

    }
}
