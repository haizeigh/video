package com.westwell.server.service;

import com.westwell.server.dto.TaskDetailInfoDto;

public interface ResultDumpService {

//    导出每一帧的结果
    void dumpFrameResult(TaskDetailInfoDto task) throws Exception;

//    导出临时底库
    void dumpTaskTemptResult(TaskDetailInfoDto task);

//    导出汇总结果
    void dumpTaskFinalResult(TaskDetailInfoDto task) throws Exception;
}
