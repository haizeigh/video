package com.westwell.server.service.impl;

import com.westwell.api.DetectionServiceGrpc;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyFacesContainer;
import com.westwell.server.service.FaceDetectionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class FaceDetectionServiceImpl implements FaceDetectionService {

    @Resource
    IdentifyFacesContainer identifyFacesContainer;

    @Resource
    DetectionServiceGrpc.DetectionServiceBlockingStub detectionServiceBlockingStub;

    @Resource
    RedisUtils redisUtils;

    @Override
    public List<String> detectFacesInPic(List<String> picKeys) {

  /*      PicsInRedisRequest.Builder builder = PicsInRedisRequest.newBuilder();
        builder.addAllPickeysReq(picKeys);

        DetectPicsInRedisResponse detectPicsInRedisResponse = detectionServiceBlockingStub.detectPicsInRedis(builder.build());
        return detectPicsInRedisResponse.getPickeysResList();*/

        List<String> list = new ArrayList<>();
        picKeys.stream().forEach(pic -> {
            String face = pic + ":1";
            redisUtils.putHash(face, "pic",redisUtils.get(pic));
            redisUtils.putHash(face, "location","location");
            redisUtils.putHash(face, "feature","feature");
            list.add(face);
        });
        return list;
    }

    @Override
    public void storeFaces(List<String> facesKeys) {
        identifyFacesContainer.addFrameFaceKeys(facesKeys);
    }
}
