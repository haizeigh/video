package com.westwell.server.service.impl;

import com.westwell.api.DetectPicsInRedisResponse;
import com.westwell.api.DetectionServiceGrpc;
import com.westwell.api.PicsInRedisRequest;
import com.westwell.server.container.IdentifyFacesContainer;
import com.westwell.server.service.FaceDetectionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FaceDetectionServiceImpl implements FaceDetectionService {


    @Resource
    DetectionServiceGrpc.DetectionServiceBlockingStub detectionServiceBlockingStub;

    @Override
    public List<String> detectFacesInPic(List<String> picKeys) {

        PicsInRedisRequest.Builder builder = PicsInRedisRequest.newBuilder();
        builder.addAllPickeysReq(picKeys);

        DetectPicsInRedisResponse detectPicsInRedisResponse = detectionServiceBlockingStub.detectPicsInRedis(builder.build());
        return detectPicsInRedisResponse.getPickeysResList();
    }

    @Override
    public void storeFaces(List<String> facesKeys) {
        IdentifyFacesContainer.addFrameFaceKeys(facesKeys);
    }
}
