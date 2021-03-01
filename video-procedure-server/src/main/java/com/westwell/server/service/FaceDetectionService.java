package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;

import java.util.List;

public interface FaceDetectionService {


//    rpc通知帧检测服务
    List<String> detectFacesInPic(List<String> picKeys);

    void storeFaces(TaskDetailInfoDto task, List<String> facesKeys);

}
