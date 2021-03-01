package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;

import java.util.Date;

public interface VideoProcessService {

    TaskDetailInfoDto detectVideo(Integer cameraNo, Date videoStartTime, Date videoEndTime) ;

}
