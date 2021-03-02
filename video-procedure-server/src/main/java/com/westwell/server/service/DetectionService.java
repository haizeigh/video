package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;

import java.util.List;

public interface DetectionService {


//    rpc通知帧检测服务
    List<String> detectFacesInPic(List<String> picKeys);

    void storePicFrames(TaskDetailInfoDto task, List<String> facesKeys);

//    检测body
    List<String> detectBodiesInPic(List<String> picKeyList);

}
