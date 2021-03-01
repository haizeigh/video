package com.westwell.server.service.impl;

import com.westwell.api.DetectPicsInRedisResponse;
import com.westwell.api.DetectionServiceGrpc;
import com.westwell.api.PicsInRedisRequest;
import com.westwell.server.container.IdentifyFacesContainer;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.FaceDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class FaceDetectionServiceImpl implements FaceDetectionService {

    @Resource
    IdentifyFacesContainer identifyFacesContainer;

    @Resource
    DetectionServiceGrpc.DetectionServiceBlockingStub detectionServiceBlockingStub;


    @Override
    public List<String> detectFacesInPic(List<String> picKeys) {

        log.info("待检测的图片数目：{}", picKeys.size());
        PicsInRedisRequest.Builder builder = PicsInRedisRequest.newBuilder();
        builder.addAllPickeysReq(picKeys);

        DetectPicsInRedisResponse detectPicsInRedisResponse = detectionServiceBlockingStub.detectPicsInRedis(builder.build());
        return detectPicsInRedisResponse.getPickeysResList();

    }

    @Override
    public void storeFaces(TaskDetailInfoDto task, List<String> facesKeys) {
        identifyFacesContainer.addFrameFaceKeys( facesKeys, task);
    }
}
