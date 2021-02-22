package com.westwell.server.service.impl;

import com.westwell.api.common.utils.DateUtils;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.exception.VPException;
import com.westwell.server.common.utils.FfmpegUtil;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.VideoMediaService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class VideoMediaServiceImpl implements VideoMediaService {


    @Resource
    RedisUtils redisUtils;

//    @Resource
//    ImgTransitionUtil imgTransitionUtil;

    @Resource
    VideoAsyncServiceImpl videoAsyncService;

    @Override
    public boolean cutVideoToPics(TaskDetailInfoDto task, String picsPath) {

        File file = new File(picsPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        StringBuilder builder = new StringBuilder("");
        builder.append("\"")
                .append("rtsp://")
                .append(task.getCameraInfoEntity().getUserName())
                .append(":")
                .append(task.getCameraInfoEntity().getPasswd())
                .append("@")
                .append(task.getCameraInfoEntity().getNvrIp())
                .append(":")
                .append(task.getCameraInfoEntity().getNvrPort())
                .append("/Streaming/tracks/")
                .append(task.getCameraInfoEntity().getCameraNo())
                .append(task.getCameraInfoEntity().getStreamNo())
                .append("?starttime=")
                .append(DateUtils.format(task.getTaskEntity().getVideoStartTime(), DateUtils.DATE_DAY))
                .append("t")
                .append(DateUtils.format(task.getTaskEntity().getVideoStartTime(), DateUtils.DATE_TIME))
                .append("z&endtime=")
                .append(DateUtils.format(task.getTaskEntity().getVideoEndTime(), DateUtils.DATE_DAY))
                .append("t")
                .append(DateUtils.format(task.getTaskEntity().getVideoEndTime(), DateUtils.DATE_TIME))
                .append("t")
                .append("\"");


        List<String> commands = new java.util.ArrayList<String>();
        commands.add(DataConfig.FFMPEG_PATH);
//        commands.add("ffmpeg");

        commands.add("-i");
        commands.add(builder.toString());

        commands.add("-t");
        long diffSec = (task.getTaskEntity().getVideoEndTime().getTime() - task.getTaskEntity().getVideoStartTime().getTime() ) / 1000 ;
        commands.add( diffSec+"");

        commands.add("-r");
        commands.add(task.getTaskEntity().getFrame()+"");

        commands.add("-q:v");
        commands.add("2");

        commands.add("-f");
        commands.add("image2");

        commands.add("-y");

        commands.add(picsPath + "/%05d.jpeg");
        StringBuffer commandsBuffer = new StringBuffer();
        for (int i = 0; i < commands.size(); i++)
            commandsBuffer.append(commands.get(i) + " ");
        System.out.println("command is : " + commandsBuffer);

        int exit = 0;
        try {

            Process process = FfmpegUtil.cutPics( commandsBuffer.toString());
            if ((exit = process.waitFor()) == 0) {
                System.out.println("---执行结果：---" + (exit == 0 ? "【成功】" : "【失败】"));
            }
        }catch (Exception e){
            throw new VPException("", e);
        }

        return exit == 0;
    }

    @Override
    public List<String> writePicsToRedis(TaskDetailInfoDto task, String picsPath) throws Exception {

        File file = new File(picsPath);
        if (!file.exists()){
            return null;
        }

        List<String> picKeyList = new ArrayList<>();
        //获取文件数组
        File[] files = file.listFiles();
        List<Future<String>> futureList = new ArrayList<>();

        //遍历文件数组，获得文件名
        for (File image : files) {
            Future<String> stringFuture = videoAsyncService.writePicsToRedis(task, image);
            futureList.add(stringFuture);
        }
//        要批量读取结果
        for (Future<String> stringFuture : futureList) {
            picKeyList.add(stringFuture.get());
        }
        return picKeyList;
    }

    @Override
    public boolean clearPicsInRedis(List<String> picKeyList) {

        picKeyList.stream().forEach(key -> redisUtils.delete(key));
        return true;
    }

}
