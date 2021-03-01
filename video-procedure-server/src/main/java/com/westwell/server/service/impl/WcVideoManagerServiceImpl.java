package com.westwell.server.service.impl;

import com.westwell.api.common.utils.DateUtils;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.enums.TaskStatusEnum;
import com.westwell.server.common.exception.VPException;
import com.westwell.server.common.utils.DateSplitUtils;
import com.westwell.server.dto.RouterCameraResultDto;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.FaceIdentifyService;
import com.westwell.server.service.ResultDumpService;
import com.westwell.server.service.VideoProcessService;
import com.westwell.server.service.WcVideoManagerService;
import lombok.extern.slf4j.Slf4j;
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
    FaceIdentifyService faceIdentifyService;

    @Override
    @Async("taskExecutor")
    public Future<RouterCameraResultDto> routerGap(Integer cameraNo) {

        Date firstTime = DateUtils.stringToDate(DataConfig.VIDEO_START_TIME);
        Date endTime = DateUtils.stringToDate(DataConfig.VIDEO_END_TIME);
        List<DateSplitUtils.DateSplit> dateSplits = DateSplitUtils.splitDate(firstTime, endTime, DateSplitUtils.IntervalType.HOUR, 1);

        List<Future<TaskDetailInfoDto>> list = new ArrayList<>();
        long start = System.currentTimeMillis();

        //分段处理
        for (DateSplitUtils.DateSplit dateSplit : dateSplits) {
            log.info("{}摄像，任务时间{}-{}开始", cameraNo, dateSplit.getStartDateTimeStr(), dateSplit.getEndDateTimeStr());
            Future<TaskDetailInfoDto> taskDetailInfoDtoFuture
                    = videoProcessService.detectVideo(cameraNo, dateSplit.getStartDateTime(), dateSplit.getEndDateTime());
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
//        long end = System.currentTimeMillis();

        //导出数据
        for (TaskDetailInfoDto task : taskDetailInfoDtoList) {
            if (!task.getTaskEntity().getTaskStatus().equals(TaskStatusEnum.SUCCESS.getCode())) {
                log.error("任务{}视频解析出错", task);
                continue;
            }

            try {

                String textPath = task.getTaskPath() + "/identify";
                resultDumpService.dumpFrameResult(task, textPath);
                resultDumpService.dumpTaskFinalResult(task, textPath);

            } catch (Exception e) {
                log.error("视频解析或者导出文件出错", e);
                throw new VPException("视频解析或者导出文件出错", e);
            } finally {
                long end = System.currentTimeMillis();
                System.out.println("总耗时 ：" + (end - start));
                log.info("清理临时数据");
                faceIdentifyService.clearVideoCache(task);
            }
        }


        RouterCameraResultDto routerCameraResultDto = new RouterCameraResultDto();
        routerCameraResultDto.setCameraNo(cameraNo);
        routerCameraResultDto.setResult(true);
        return new AsyncResult(routerCameraResultDto);


    }


}
