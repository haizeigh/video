package com.westwell.server.service.impl;

import com.westwell.server.dto.RouterCameraResultDto;
import com.westwell.server.service.WcTaskManagerService;
import com.westwell.server.service.WcVideoManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Slf4j
@Service
public class WcTaskManagerServiceImpl implements WcTaskManagerService {

    @Resource
    WcVideoManagerService videoManagerService;

    @Override
    public void routerCamera(List<Integer> cameraNos) {

        List<Future<RouterCameraResultDto>> list = new ArrayList<>();
        cameraNos.stream().forEach( cameraNo -> {
            Future<RouterCameraResultDto> booleanFuture = videoManagerService.routerGap(cameraNo);
            list.add(booleanFuture);
        });


        long start = System.currentTimeMillis();
        for (Future<RouterCameraResultDto> future : list) {
            try {
                RouterCameraResultDto routerCameraResultDto = future.get();
                log.info("{}摄像任务结束", routerCameraResultDto);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        long end = System.currentTimeMillis();
        log.info("总的任务{}耗时{}", cameraNos, ( end - start));
    }
}
