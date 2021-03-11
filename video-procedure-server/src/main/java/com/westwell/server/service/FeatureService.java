package com.westwell.server.service;

import com.westwell.server.dto.CompareSimilarityDto;
import com.westwell.server.dto.TaskDetailInfoDto;

import java.util.List;

public interface FeatureService {


//    rpc调用特征提取服务
    boolean extractFaceFeature(List<String> picKeys);

    //    rpc调用特征提取服务
    boolean extractBodyFeature(List<String> picKeys);

//    rpc通知1:N人脸对比服务
    CompareSimilarityDto compareFaceWithCollection(TaskDetailInfoDto task, String faceKey) throws Exception;

    //    rpc通知1:N人脸对比服务
    CompareSimilarityDto compareBodyWithCollection(TaskDetailInfoDto task, String bodyKey) throws Exception;

//    rpc通知N:M人脸对比服务
    String compareCollectionWithStudent(TaskDetailInfoDto task, String faceColle) throws Exception;

    //    rpc通知1:N人脸对比服务
    CompareSimilarityDto comparePicWithCollection(TaskDetailInfoDto task, String picKey) throws Exception;

}
