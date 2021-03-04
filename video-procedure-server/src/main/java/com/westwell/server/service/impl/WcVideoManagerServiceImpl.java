package com.westwell.server.service.impl;

import com.westwell.api.common.utils.DateUtils;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.DateSplitUtils;
import com.westwell.server.dto.RouterCameraResultDto;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

@Slf4j
@Service
public class WcVideoManagerServiceImpl implements WcVideoManagerService {

    @Resource
    VideoProcessService videoProcessService;

    @Resource
    ResultDumpService resultDumpService;

    @Resource
    IdentifyService identifyService;

    @Resource
    VideoMediaService videoMediaService;

    @Override
    @Async("taskExecutor")
    public Future<RouterCameraResultDto> routerGap(Integer cameraNo, Integer taskNo) {

        Date firstTime = DateUtils.stringToDate(DataConfig.VIDEO_START_TIME);
        Date endTime = DateUtils.stringToDate(DataConfig.VIDEO_END_TIME);
        List<DateSplitUtils.DateSplit> dateSplits = DateSplitUtils.splitDate(firstTime, endTime, DateSplitUtils.IntervalType.MINUTE, DataConfig.INTERVAL_MINUTE);

        List<Future<TaskDetailInfoDto>> list = new ArrayList<>();
        long start = System.currentTimeMillis();

        //分段处理
        for (DateSplitUtils.DateSplit dateSplit : dateSplits) {
            log.info("{}摄像，任务时间{}-{}开始", cameraNo, dateSplit.getStartDateTimeStr(), dateSplit.getEndDateTimeStr());
            Future<TaskDetailInfoDto> taskDetailInfoDtoFuture
                    = videoProcessService.detectVideo(taskNo, cameraNo, dateSplit.getStartDateTime(), dateSplit.getEndDateTime());
            list.add(taskDetailInfoDtoFuture);

        }

//        搜集结果
        List<TaskDetailInfoDto> taskDetailInfoDtoList = new ArrayList<>();
        for (Future<TaskDetailInfoDto> future : list) {
            try {
                TaskDetailInfoDto task = future.get();
                taskDetailInfoDtoList.add(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        RouterCameraResultDto routerCameraResultDto = new RouterCameraResultDto();
        routerCameraResultDto.setCameraNo(cameraNo);
//        long end = System.currentTimeMillis();
        //导出数据
        if (CollectionUtils.isEmpty(taskDetailInfoDtoList)){
            log.info("任务全部失败");
            routerCameraResultDto.setResult(false);
            return new AsyncResult(routerCameraResultDto);
        }

        boolean flag = true;
        TaskDetailInfoDto fistTask = taskDetailInfoDtoList.get(0);
        for (TaskDetailInfoDto.TaskType taskType : TaskDetailInfoDto.TaskType.values()) {

            try {

                fistTask.setTaskType(taskType);
                log.info("输出临时底库无标签的图片");
                videoMediaService.readPicCollesFromRedis(fistTask);

                log.info("输出临时底库有标签的图片");
                videoMediaService.readfacesCollesFromRedis(fistTask);

                String textPath = fistTask.getTaskDumpPath();
                resultDumpService.dumpFrameResult(fistTask, textPath);
                resultDumpService.dumpTaskFinalResult(fistTask, textPath);

            } catch (Exception e) {
                log.error("任务{}导出文件出错", fistTask, e);
                flag = false;
//                throw new VPException("视频解析或者导出文件出错", e);
            } finally {
                videoProcessService.clearVideoCache(fistTask);
            }

        }
        long end = System.currentTimeMillis();
        System.out.println("总耗时 ：" + (end - start));
        log.info("清理临时数据");


        routerCameraResultDto.setResult(flag);
        return new AsyncResult(routerCameraResultDto);


    }


}
