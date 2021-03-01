package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;

import java.util.List;

public interface VideoMediaService {

//    剪切视频的帧
    boolean cutVideoToPics(TaskDetailInfoDto task, String picsPath);

//    把图片保存到redis
    List<String> writePicsToRedis(TaskDetailInfoDto task, String picsPath) throws Exception;

    void readPicsFromRedis(TaskDetailInfoDto task, List<String> faceKeys) throws Exception;

    boolean clearListInRedis(List<String> picKeyList);

    void readPicCollesFromRedis(TaskDetailInfoDto task);

    void readBasePicsFromRedis(TaskDetailInfoDto task);
}
