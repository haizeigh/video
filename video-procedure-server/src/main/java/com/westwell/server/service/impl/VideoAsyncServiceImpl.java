package com.westwell.server.service.impl;

import com.westwell.api.common.utils.ImgTransitionUtil;
import com.westwell.server.common.configs.DataConfig;
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
        System.out.println(Thread.currentThread().getName() + " 单次写入redis耗时 ：" + (end - start) );
        return new AsyncResult<String>(picKey);
    }

    String getPicKey(TaskDetailInfoDto task, File image){
//        计算当前图的时间
        Double frame = Double.parseDouble(task.getTaskEntity().getFrame());
        long imageNum = Long.parseLong(image.getName().split("\\.")[0]);
        long milSec = (long) ((imageNum - 1) / frame * 1000);

//        计算图的帧序号
        long frameNum = imageNum  - (long)((imageNum - 1) / frame * frame);


//        wellcare:任务编号:camera_编号:时间戳:帧编号
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(DataConfig.TASK_PREFIX)
                .append(":")
                .append(task.getTaskEntity().getTaskNo())
                .append(":")
                .append(task.getCameraInfoEntity().getCameraNo())
                .append(":")
                .append( (task.getTaskEntity().getVideoStartTime().getTime() + milSec) / 1000 )
                .append(":")
                .append(frameNum);

        String newImageName = stringBuilder.toString();
//        System.out.println(image.getParent() + "/" + newImageName);
        File newImage = new File(image.getParent() + "/" + newImageName);
        image.renameTo(newImage);

        return newImageName;
    }

    public static void main(String[] args) {
        System.out.println(Long.parseLong("00030"));

        File file = new File("/home/westwell/java/file/identify/2021-03-04/1-38/113000-113100/pic/000000006.jpeg");
        System.out.println(file.getParent());
    }
}
