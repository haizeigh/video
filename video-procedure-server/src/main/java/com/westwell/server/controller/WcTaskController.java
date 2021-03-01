package com.westwell.server.controller;


import com.westwell.api.common.utils.DateUtils;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.enums.TaskStatusEnum;
import com.westwell.server.common.exception.VPException;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.entity.WcTaskEntity;
import com.westwell.server.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;


/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
@Slf4j
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

    @Resource
    VideoProcessService videoProcessService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 列表
     */
    @RequestMapping("/list")
    public WcTaskEntity list(@RequestParam Map<String, Object> params) {

        WcTaskEntity wcTaskEntity = wcTaskService.queryOne();


        long start = System.currentTimeMillis();
        TaskDetailInfoDto task = null;
        log.info("开始启动");

        try {
            task = videoProcessService.detectVideo(DataConfig.CAMERA_NO
                    , DateUtils.stringToDate(DataConfig.VIDEO_START_TIME)
                    , DateUtils.stringToDate(DataConfig.VIDEO_END_TIME));
            log.info("本次任务{}", task);

            if ( !task.getTaskEntity().getTaskStatus().equals(TaskStatusEnum.SUCCESS.getCode())){
                log.error("视频解析出错");
                return wcTaskEntity;
            }

//            导出数据
            String textPath = DataConfig.IDENTIFY_CACHE_PATH
                    + "/" + DateUtils.format(new Date(), DateUtils.DATE_PATTERN)
                    + "/" + task.getTaskEntity().getCameraNo()
                    + "/" + DateUtils.format(task.getTaskEntity().getVideoStartTime(), DateUtils.DATE_TIME) ;

            resultDumpService.dumpFrameResult(task, textPath);
            resultDumpService.dumpTaskFinalResult(task, textPath);


        } catch (Exception e) {
            log.error("视频解析或者导出文件出错", e);
            throw new VPException("视频解析或者导出文件出错", e);
        }finally {
            long end = System.currentTimeMillis();
            System.out.println("总耗时 ：" + (end - start));
            log.info("清理临时数据");
            faceIdentifyService.clearVideoCache(task);
        }
        return wcTaskEntity;
    }




}
