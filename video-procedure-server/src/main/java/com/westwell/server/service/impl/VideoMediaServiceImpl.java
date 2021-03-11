package com.westwell.server.service.impl;

import com.google.common.base.Strings;
import com.westwell.api.common.utils.DateUtils;
import com.westwell.api.common.utils.ImgTransitionUtil;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.exception.VPException;
import com.westwell.server.common.utils.FfmpegUtil;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainerManager;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.VideoMediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Slf4j
@Service
public class VideoMediaServiceImpl implements VideoMediaService {


    @Resource
    RedisUtils redisUtils;


    @Resource
    VideoAsyncServiceImpl videoAsyncService;

    @Resource
    IdentifyContainerManager identifyContainerManager;

    @Override
    public boolean cutVideoToPics(TaskDetailInfoDto task) {

        long start = System.currentTimeMillis();
        String picsPath  = task.getPicPath();
        File file = new File(picsPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        List<String> commands = new java.util.ArrayList<String>();
        commands.add(DataConfig.FFMPEG_PATH);
//        commands.add("ffmpeg");

        commands.add("-i");
        commands.add(task.getVideoFullPath());

//        commands.add("-s");
//        commands.add(DataConfig.PIXELS);

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

        commands.add(picsPath + "/" + task.getPicName());
        StringBuffer commandsBuffer = new StringBuffer();
        for (int i = 0; i < commands.size(); i++)
            commandsBuffer.append(commands.get(i) + " ");
        log.info("cut pic command is : " + commandsBuffer);

        int exit = 0;
        try {

            Process process = FfmpegUtil.exec( commandsBuffer.toString());
            if ((exit = process.waitFor()) == 0) {
                log.info("---执行结果：---" + (exit == 0 ? "【成功】" : "【失败】"));
            }
        }catch (Exception e){
            throw new VPException("ffmpeg视频工具异常", e);
        }
        long end = System.currentTimeMillis();
        log.info("剪切图片耗时间{}ms", end-start);

        return exit == 0;
    }

    @Override
    public List<String> writePicsToRedis(TaskDetailInfoDto task) throws Exception {

        long start = System.currentTimeMillis();
        String picsPath = task.getPicPath();
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
        long end = System.currentTimeMillis();
        log.info("写图片到redis耗时间{}ms", end-start);

        return picKeyList;
    }

    @Override
    public void readPicsFromRedis(TaskDetailInfoDto task, List<String> faceKeys, String path) throws Exception {

        faceKeys.stream().forEach(faceKey -> {

            String pic = redisUtils.getHash(faceKey, DataConfig.PIC).toString();
            try {
//                ImgTransitionUtil.base64ToFile(pic, task.getTaskTemptPath() + "/faces/" + faceKey + ".jpeg");
                ImgTransitionUtil.base64ToFile(pic, path + "/" + faceKey + ".jpeg");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void readPicFromRedis(String picKey, String path) throws Exception {
        String pic = redisUtils.getHash(picKey, DataConfig.PIC).toString();
        try {
            ImgTransitionUtil.base64ToFile(pic, path + "/" + picKey + ".jpeg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean clearListInRedis(List<String> picKeyList) {

        picKeyList.stream().forEach(key -> redisUtils.delete(key));
        return true;
    }



    @Override
    public void readBasePicsFromRedis(TaskDetailInfoDto task) {

        String base = DataConfig.WELL_CARE_BASE;
        Map<String, Object> hash = redisUtils.getHash(base);
        hash.forEach( (num,imageKeys ) -> {
            String[] keys = imageKeys.toString().split(",");

            Arrays.stream(keys).forEach(faceKey -> {
                String pic = redisUtils.getHash(faceKey, DataConfig.PIC).toString();
                try {
//                    ImgTransitionUtil.base64ToFile(pic, DataConfig.FACE_PIC_CACHE_PATH + "/" + base+"/" + faceKey.substring(faceKey.lastIndexOf("/") +1) + ".jpeg");
                    ImgTransitionUtil.base64ToFile(pic, task.getTaskTemptPath() + "/" + base+"/" + faceKey.substring(faceKey.lastIndexOf("/") +1) );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Override
    public boolean readVideo(TaskDetailInfoDto task) {

        long start = System.currentTimeMillis();
        String videoPath = task.getVideoPath();

        File file = new File(videoPath);
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

        commands.add("-c");
        commands.add("copy");

        commands.add("-y");

        commands.add(task.getVideoFullPath());
        StringBuffer commandsBuffer = new StringBuffer();
        for (int i = 0; i < commands.size(); i++)
            commandsBuffer.append(commands.get(i) + " ");
        log.info("video command is : " + commandsBuffer);

        int exit = 0;
        try {

            Process process = FfmpegUtil.exec( commandsBuffer.toString());
            if ((exit = process.waitFor()) == 0) {
                log.info("---执行结果：---" + (exit == 0 ? "【成功】" : "【失败】"));
            }
        }catch (Exception e){
            throw new VPException("ffmpeg视频工具异常", e);
        }
        long end = System.currentTimeMillis();
        log.info("下载视频耗时间{}ms", end-start);

        return exit == 0;
    }


    @Override
    public void readPicCollesFromRedis(TaskDetailInfoDto task) {


            String picCachePath = task.getTaskTemptPathForCollection();
            log.info("临时底库的地址" + picCachePath);

            List<String> picCollection = identifyContainerManager.picColleKeys(task);
            if (CollectionUtils.isEmpty(picCollection)){
                log.info("任务no={}没有底库数据",task.getTaskEntity().getTaskNo() );
                return;
            }
            picCollection.forEach( picColle -> {
                List<String> picsFromBucket = identifyContainerManager.getPicsFromBucket(picColle);
                picsFromBucket.forEach(faceKey -> {
                    String pic = redisUtils.getHash(faceKey, DataConfig.PIC).toString();
                    try {
                        ImgTransitionUtil.base64ToFile(pic, picCachePath + "/" + picColle+"/" + faceKey + ".jpeg");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            });
    }

    @Override
    public void readLabelPicCollesFromRedis(TaskDetailInfoDto task) {


            String labelPicCachePath = task.getTaskTemptPathForLabelCollection() ;
            log.info("临时底库有标签的地址" + labelPicCachePath);

            List<String> picCollection = identifyContainerManager.picColleKeys(task);
            if (CollectionUtils.isEmpty(picCollection)){
                log.info("任务no={}没有底库数据",task.getTaskEntity().getTaskNo() );
                return;
            }

            picCollection.forEach( picColle -> {

                String identify = identifyContainerManager.getIdentify(picColle, task);
                if (Strings.isNullOrEmpty(identify)){
//                无标签
                    return;
                }

                List<String> picsFromBucket = identifyContainerManager.getPicsFromBucket(picColle);
                picsFromBucket.forEach(faceKey -> {
                    String pic = redisUtils.getHash(faceKey, DataConfig.PIC).toString();
                    try {
                        ImgTransitionUtil.base64ToFile(pic, labelPicCachePath + "/" + identify+"/" + faceKey + ".jpeg");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            });
        }

}
