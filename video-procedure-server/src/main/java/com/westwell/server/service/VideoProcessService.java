package com.westwell.server.service;

import java.util.List;

public interface VideoProcessService {

    Object creatTask();

    Boolean cutVideoToPics(Object task, String videoPath, String picsPath);

    List<String> writePicsToRedis(Object task, String picsPath);

    List<String> detectPics(List<String> picKeys);

    Boolean callPicsFeature(List<String> picKeys);

    void picsCompareCollection();

    void identifyCollection();

    void downLoadData();

}
