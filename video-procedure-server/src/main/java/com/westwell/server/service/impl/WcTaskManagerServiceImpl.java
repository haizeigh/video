package com.westwell.server.service.impl;

import com.westwell.server.common.enums.TaskStatusEnum;
import com.westwell.server.dao.WcTaskManagerDao;
import com.westwell.server.dto.RouterCameraResultDto;
import com.westwell.server.entity.WcTaskManagerEntity;
import com.westwell.server.service.WcTaskManagerService;
import com.westwell.server.service.WcVideoManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

@Slf4j
@Service
public class WcTaskManagerServiceImpl implements WcTaskManagerService {

    @Resource
    WcVideoManagerService videoManagerService;

    @Resource
    WcTaskManagerDao taskManagerDao;

    @Override
    public void routerCamera(List<Integer> cameraNos) {

//        先默认创建任务
        WcTaskManagerEntity taskManagerEntity = WcTaskManagerEntity.builder()
                .startTime(new Date())
                .status(TaskStatusEnum.DOING.getCode())
                .build();
        taskManagerDao.insertSelective(taskManagerEntity);

//        分配任务
        List<Future<RouterCameraResultDto>> list = new ArrayList<>();
        cameraNos.stream().forEach( cameraNo -> {
            Future<RouterCameraResultDto> booleanFuture = videoManagerService.routerGap(cameraNo, taskManagerEntity.getId());
            list.add(booleanFuture);
        });


        long start = System.currentTimeMillis();
        taskManagerEntity.setStatus(TaskStatusEnum.SUCCESS.getCode());

        for (Future<RouterCameraResultDto> future : list) {
            try {
                RouterCameraResultDto routerCameraResultDto = future.get();
                log.info("{}摄像任务结束", routerCameraResultDto);

            } catch (Exception e) {
                e.printStackTrace();
                taskManagerEntity.setStatus(TaskStatusEnum.PART.getCode());
            }

        }

        taskManagerEntity.setEndTime(new Date());
        taskManagerDao.updateByPrimaryKey(taskManagerEntity);
        long end = System.currentTimeMillis();
        log.info("总的任务{}耗时{}", cameraNos, ( end - start));

    }
}
