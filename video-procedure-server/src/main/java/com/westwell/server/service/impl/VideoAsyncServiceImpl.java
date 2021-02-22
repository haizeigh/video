package com.westwell.server.service.impl;

import com.westwell.api.common.utils.ImgTransitionUtil;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.dto.TaskDetailInfoDto;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.concurrent.Future;

@Service
public class VideoAsyncServiceImpl {


    @Resource
    RedisUtils redisUtils;

    @Async("taskExecutor")
    public Future<String> writePicsToRedis(TaskDetailInfoDto task, File image) throws Exception {

        long start = System.currentTimeMillis();

        String fileToBase64 = ImgTransitionUtil.imageFileToBase64(image);
        String picKey = getPicKey(task, image);
        redisUtils.set(picKey, fileToBase64);

        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " 耗时 ：" + (end - start) );
        return new AsyncResult<String>(picKey);
    }

    String getPicKey(TaskDetailInfoDto task, File image){
//        wellcare:任务编号:camera_编号:时间戳:帧编号
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" wellcare:")
                .append(task.getTaskEntity().getTaskNo())
                .append(":")
                .append(task.getCameraInfoEntity().getCameraNo())
                .append(":")
                .append(task.getTaskEntity().getTaskStartTime().getTime())
                .append(":")
                .append(image.getName());

        return stringBuilder.toString();
    }
}
