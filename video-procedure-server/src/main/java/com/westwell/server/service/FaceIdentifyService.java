package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;

import java.util.List;

public interface FaceIdentifyService {


//    识别人物
    boolean identifyFaces(TaskDetailInfoDto task, List<String> faceKeyList);


}
