package com.westwell.server.service.impl;

import com.westwell.api.DetectPicsInRedisResponse;
import com.westwell.api.DetectionServiceGrpc;
import com.westwell.api.PicsInRedisRequest;
import com.westwell.server.container.IdentifyContainerManager;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.DetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class DetectionServiceImpl implements DetectionService {

    @Resource
    IdentifyContainerManager identifyContainerManager;

    @Resource
    DetectionServiceGrpc.DetectionServiceBlockingStub detectionServiceBlockingStub;


    @Override
    public List<String> detectFacesInPic(List<String> picKeys) {

        log.info("待检测的face图片数目：{}, 第一个{}", picKeys.size(), picKeys.get(0));
        PicsInRedisRequest.Builder builder = PicsInRedisRequest.newBuilder();
        builder.addAllPickeysReq(picKeys);

        DetectPicsInRedisResponse detectPicsInRedisResponse = detectionServiceBlockingStub.detectPicsInRedis(builder.build());
        return detectPicsInRedisResponse.getPickeysResList();

    }

    @Override
    public void storePicFrames(TaskDetailInfoDto task, List<String> picKeys) {
        identifyContainerManager.addPicFrameKeys( picKeys, task);
    }

    @Override
    public List<String> detectBodiesInPic(List<String> picKeyList) {

        log.info("待检测的body图片数目：{}", picKeyList.size());
        PicsInRedisRequest.Builder builder = PicsInRedisRequest.newBuilder();
        builder.addAllPickeysReq(picKeyList);

        DetectPicsInRedisResponse detectPicsInRedisResponse = detectionServiceBlockingStub.detectPicsBodyInRedis(builder.build());
        return detectPicsInRedisResponse.getPickeysResList();
    }

}
