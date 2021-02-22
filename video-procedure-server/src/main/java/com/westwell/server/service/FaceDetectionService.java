package com.westwell.server.service;

import java.util.List;

public interface FaceDetectionService {


//    rpc通知帧检测服务
    List<String> detectFacesInPic(List<String> picKeys);

    void storeFaces(List<String> facesKeys);

}
