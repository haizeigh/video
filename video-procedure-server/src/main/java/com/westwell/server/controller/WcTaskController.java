package com.westwell.server.controller;


import com.westwell.api.common.utils.DateUtils;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.enums.TaskStatusEnum;
import com.westwell.server.common.exception.VPException;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.entity.WcTaskEntity;
import com.westwell.server.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;


/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
@RestController
@RequestMapping("wctask")
public class WcTaskController {

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
    ResultDumpService resultDumpService;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public WcTaskEntity list(@RequestParam Map<String, Object> params) {

        WcTaskEntity wcTaskEntity = wcTaskService.queryOne();

        //清理redis
        Set<String> keys = redisTemplate.keys("*");
        videoMediaService.clearPicsInRedis(new ArrayList<>(keys));


        long start = System.currentTimeMillis();
        TaskDetailInfoDto task = null;
        try {
//            处理截图
//            todo 分配开始时间 camera编号
            task = wcTaskService.getOneTaskDetailInfoDto();

            String picPath = DataConfig.PIC_CACHE_PATH
                    + "/" + DateUtils.format(new Date(), DateUtils.DATE_PATTERN)
                    + "/" + task.getTaskEntity().getCameraNo()
                    + "/" + DateUtils.format(task.getTaskEntity().getVideoStartTime(), DateUtils.DATE_TIME) ;
            videoMediaService.cutVideoToPics(task, picPath);

//           todo FFMPEG_PATH配置
            List<String> picKeyList = videoMediaService.writePicsToRedis(task, picPath);
            System.out.println(picKeyList);

//            检测人脸
            List<String> faceKeyList = faceDetectionService.detectFacesInPic(picKeyList);
//            保存面部图
            faceDetectionService.storeFaces(faceKeyList);
            videoMediaService.clearPicsInRedis(picKeyList);

//            特征提取
            faceFeatureService.extractFaceFeature(faceKeyList);

//            底库创建以及识别人物
            faceIdentifyService.identifyFaces(task, faceKeyList);

//            导出数据
            String textPath = DataConfig.IDENTIFY_CACHE_PATH
                    + "/" + DateUtils.format(new Date(), DateUtils.DATE_PATTERN)
                    + "/" + task.getTaskEntity().getCameraNo()
                    + "/" + DateUtils.format(task.getTaskEntity().getVideoStartTime(), DateUtils.DATE_TIME) ;

            resultDumpService.dumpFrameResult(task, textPath);
            resultDumpService.dumpTaskFinalResult(task, textPath);



        } catch (Exception e) {
            e.printStackTrace();
            if (task != null && task.getTaskEntity() != null){
                UpdateTaskStatus(task, TaskStatusEnum.FAIL);
            }
            throw new VPException("解析视频出现错误", e);
        }

        faceIdentifyService.clearContainerCache();
        UpdateTaskStatus(task, TaskStatusEnum.SUCCESS);
        long end = System.currentTimeMillis();
        System.out.println("总耗时 ：" + (end - start));
        return wcTaskEntity;
    }

    private void UpdateTaskStatus(TaskDetailInfoDto task, TaskStatusEnum taskStatusEnum) {
        WcTaskEntity taskEntity = task.getTaskEntity();
        taskEntity.setTaskStatus(taskStatusEnum.getCode());
        taskEntity.setTaskEndTime(new Date());
        wcTaskService.update(taskEntity);
    }


}
