package com.westwell.server.service;

import com.westwell.server.dto.CompareSimilarityDto;

import java.util.List;
import java.util.Map;

public interface FaceFeatureService {


//    rpc调用特征提取服务
    boolean extractFaceFeature(List<String> picKeys);

//    rpc通知1:N人脸对比服务
    CompareSimilarityDto compareFaceWithCollection(String faceKey) throws Exception;

//    rpc通知N:M人脸对比服务
    Map<String, String> compareCollectionWithStudent(List<String> faceColles);

    String compareCollectionWithStudent(String faceColle);

}
