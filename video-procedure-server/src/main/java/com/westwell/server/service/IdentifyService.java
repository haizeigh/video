package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;

import java.util.List;

public interface IdentifyService {


//    根据face识别人物
    boolean identifyFaces(TaskDetailInfoDto task, List<String> faceKeyList) throws Exception;


    //    根据body识别人物
    boolean identifyBody(TaskDetailInfoDto task, List<String> bodyKeyList)  throws Exception;

    //    根据face 或者 body 识别人物
    boolean identifyPic(TaskDetailInfoDto task, List<String> picKeyList)  throws Exception;
}
