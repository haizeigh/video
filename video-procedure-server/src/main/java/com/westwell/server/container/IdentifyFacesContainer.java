package com.westwell.server.container;

import com.google.common.base.Strings;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.event.FaceCollectionChangeEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@Data
public class IdentifyFacesContainer {

    private  Map<String, VideoContainer> identifyMap = new ConcurrentHashMap<>( 32 );

    @Resource
    private  ApplicationContext applicationContext;

    @Resource
    private RedisUtils redisUtils;

    public void initContainer(){
        identifyMap = new ConcurrentHashMap<>( 32 );
    }


    public boolean addPicToExistBucket(String faceKey, String faceColleKey, TaskDetailInfoDto task){

        redisUtils.lPush(faceColleKey, faceKey);
        FaceCollectionChangeEvent change = new FaceCollectionChangeEvent(task, faceColleKey);
        applicationContext.publishEvent(change);
        return true;
    }

    public  boolean addPicToNewBucket(String faceKey, TaskDetailInfoDto task) {

        String taskCameraPrefix = task.getTaskCameraPrefix();
        if (!identifyMap.containsKey(taskCameraPrefix)){
            identifyMap.put(taskCameraPrefix, new VideoContainer());
        }
//        获取容器
        VideoContainer videoContainer = identifyMap.get(taskCameraPrefix);

        String faceColleKey = videoContainer.newBucketName(task);
        if (Strings.isNullOrEmpty(faceColleKey)){
            log.warn("摄像任务{}不能创建新底库", taskCameraPrefix);
            return false;
        }

        videoContainer.getFaceCollection().add(faceColleKey);
        return addPicToExistBucket( faceKey, faceColleKey, task);
    }

    public  List<String> getPicsFromBucket(String faceColleKey){
        return redisUtils.lGetAll(faceColleKey).stream().map(Object::toString).collect(Collectors.toList());
    }

//    查找身份
    public  String getIdentify(String colleKey, TaskDetailInfoDto task){

        VideoContainer videoContainer = identifyMap.get(task.getTaskCameraPrefix());
        return videoContainer.getIdentifyMap().get(colleKey);
    }

//    写入确定的身份
    public  void addIdentify(String colleKey, String studentId, TaskDetailInfoDto task){
        VideoContainer videoContainer = identifyMap.get(task.getTaskCameraPrefix());
        videoContainer.getIdentifyMap().put(colleKey, studentId);
    }

    public  List<String> faceColleKeys(TaskDetailInfoDto task) {
        VideoContainer videoContainer = identifyMap.get(task.getTaskCameraPrefix());
        return videoContainer.getFaceCollection();
    }

    public  boolean containsKey(String faceColleKey, TaskDetailInfoDto task) {
        VideoContainer videoContainer = identifyMap.get(task.getTaskCameraPrefix());
        return videoContainer.getFaceCollection().indexOf(faceColleKey) > 0;
    }

//    查找所有排序的帧
    public  List<String> getSortedFaceKeys(TaskDetailInfoDto task){

        VideoContainer videoContainer = identifyMap.get(task.getTaskCameraPrefix());
        List<String> faces = videoContainer.getAllFrame();
        Collections.sort(faces);
        return faces;
    }

    public  void addFrameFaceKeys(List<String> faceKeys, TaskDetailInfoDto task){

//        initBucket(faceKeys, task);
        String taskCameraPrefix = task.getTaskCameraPrefix();
        if (!identifyMap.containsKey(task.getTaskCameraPrefix())){
            identifyMap.put(taskCameraPrefix, new VideoContainer());
        }
        VideoContainer videoContainer = identifyMap.get(task.getTaskCameraPrefix());
        videoContainer.getAllFrame().addAll(faceKeys);
    }



    public void initBucket(List<String> faceKeys, TaskDetailInfoDto task) {


        String taskCameraPrefix = task.getTaskCameraPrefix();
        if (!identifyMap.containsKey(taskCameraPrefix) || identifyMap.get(taskCameraPrefix).getFaceCollection().size() == 0 ){
            synchronized (this){
                log.info("初始化容器");
                if (!identifyMap.containsKey(taskCameraPrefix) || identifyMap.get(taskCameraPrefix).getFaceCollection().size() == 0 ){
                    addPicToNewBucket(faceKeys.get(0), task);
      /*
                    List<String> newFaceKeys = new ArrayList<>();
                    for (int i = 1; i < faceKeys.size(); i++) {
                        newFaceKeys.add(faceKeys.get(i));
                    }
                    faceKeys = newFaceKeys;*/
                }
            }
        }
    }

    public Map<String, String> getVideoIdentifyMap(TaskDetailInfoDto task) {

        return identifyMap.get(task.getTaskCameraPrefix()).getIdentifyMap();
    }



  /*  public static void main(String[] args) {
        List<String> sortedFaceKeys = new ArrayList<>();
        sortedFaceKeys.add("b");
        sortedFaceKeys.add("a");
        System.out.println(sortedFaceKeys);
        Collections.sort(sortedFaceKeys);
        System.out.println(sortedFaceKeys);
    }*/

}
