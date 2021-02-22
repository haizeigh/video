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
//        计算当前图的时间
        Integer frame = task.getTaskEntity().getFrame();
        long imageNum = Long.parseLong(image.getName().split("\\.")[0]);
        long milSec = (imageNum - 1) / frame * 1000;

//        计算图的帧序号
        long frameNum = imageNum  - (imageNum - 1) / frame * frame;


//        wellcare:任务编号:camera_编号:时间戳:帧编号
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" wellcare:")
                .append(task.getTaskEntity().getTaskNo())
                .append(":")
                .append(task.getCameraInfoEntity().getCameraNo())
                .append(":")
                .append(task.getTaskEntity().getTaskStartTime().getTime() + milSec )
                .append(":")
                .append(frameNum);

        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        System.out.println(Long.parseLong("00030"));
    }
}
