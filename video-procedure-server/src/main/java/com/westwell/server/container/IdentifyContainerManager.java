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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@Data
public class IdentifyContainerManager {

//    每一路相机 区分body 和 face
    private  Map<String, VideoContainer> videoContainerMap = new ConcurrentHashMap<>( 32 );


//    private Map<String, SynchronizedSet> bodyColleAndFaceColleRelationMap = new ConcurrentHashMap<>(DataConfig.CLUSTER_NUM * 2);
//    private Map<String, VideoContainer> identifyMap = new ConcurrentHashMap<>(DataConfig.CLUSTER_NUM * 2);
//    private Map<String, String> identifyMap = new ConcurrentHashMap<>(DataConfig.CLUSTER_NUM * 2);

    @Resource
    private  ApplicationContext applicationContext;

    @Resource
    private RedisUtils redisUtils;

    public void initContainer(){
        videoContainerMap = new ConcurrentHashMap<>( 32 );
    }


    public boolean addPicToExistBucket(String picKey, String picColleKey, TaskDetailInfoDto task){

        redisUtils.lPush(picColleKey, picKey);
        FaceCollectionChangeEvent change = new FaceCollectionChangeEvent(task, picColleKey, picKey);
        applicationContext.publishEvent(change);
        return true;
    }

    public  boolean addPicToNewBucket(String picKey, TaskDetailInfoDto task) {

        String taskCameraPrefix = task.getTaskCameraPrefix();
        if (!videoContainerMap.containsKey(taskCameraPrefix)){
            videoContainerMap.put(taskCameraPrefix, new VideoContainer());
        }
//        获取容器
        VideoContainer videoContainer = videoContainerMap.get(taskCameraPrefix);

        String picColleKey = videoContainer.newBucketName(task);
        if (Strings.isNullOrEmpty(picColleKey)){
            log.warn("摄像任务{}不能创建新底库", taskCameraPrefix);
            return false;
        }

        videoContainer.getPicCollection().add(picColleKey);
        return addPicToExistBucket( picKey, picColleKey, task);
    }

    public  List<String> getPicsFromBucket(String faceColleKey){
        return redisUtils.lGetAll(faceColleKey).stream().map(Object::toString).collect(Collectors.toList());
    }

//    查找身份
    public  String getIdentify(String colleKey, TaskDetailInfoDto task){

        VideoContainer videoContainer = videoContainerMap.get(task.getTaskCameraPrefix());
        return videoContainer.getIdentifyMap().get(colleKey);
    }

//    写入确定的身份
    public  void addIdentify(String colleKey, String studentId, TaskDetailInfoDto task){
        VideoContainer videoContainer = videoContainerMap.get(task.getTaskCameraPrefix());
        videoContainer.getIdentifyMap().put(colleKey, studentId);
    }

    public  List<String> picColleKeys(TaskDetailInfoDto task) {
        VideoContainer videoContainer = videoContainerMap.get(task.getTaskCameraPrefix());
        if (videoContainer == null){
            return null;
        }
        return videoContainer.getPicCollection();
    }

    public  boolean containsKey(String faceColleKey, TaskDetailInfoDto task) {
        VideoContainer videoContainer = videoContainerMap.get(task.getTaskCameraPrefix());
        return videoContainer.getPicCollection().indexOf(faceColleKey) > 0;
    }

//    查找所有排序的帧
    public  List<String> getSortedFaceKeys(TaskDetailInfoDto task){

        VideoContainer videoContainer = videoContainerMap.get(task.getTaskCameraPrefix());
        if (videoContainer == null){
            return null;
        }
        List<String> faces = videoContainer.getAllFrame();
        Collections.sort(faces);
        return faces;
    }

    public  void addPicFrameKeys(List<String> picKeys, TaskDetailInfoDto task){

        String taskCameraPrefix = task.getTaskCameraPrefix();
        if (!videoContainerMap.containsKey(task.getTaskCameraPrefix())){
            synchronized (this){
                if (!videoContainerMap.containsKey(task.getTaskCameraPrefix())){
                    videoContainerMap.put(taskCameraPrefix, new VideoContainer());
                }
            }
        }
        VideoContainer videoContainer = videoContainerMap.get(task.getTaskCameraPrefix());
        videoContainer.getAllFrame().addAll(picKeys);
    }



    public boolean initBucket(List<String> picKeys, TaskDetailInfoDto task) {

        String taskCameraPrefix = task.getTaskCameraPrefix();
        if (!videoContainerMap.containsKey(taskCameraPrefix) || videoContainerMap.get(taskCameraPrefix).getPicCollection().size() == 0 ){
            synchronized (this){
                log.info("初始化容器");
                if (!videoContainerMap.containsKey(taskCameraPrefix) || videoContainerMap.get(taskCameraPrefix).getPicCollection().size() == 0 ){
                    addPicToNewBucket(picKeys.get(0), task);
                    return true;

                }
                return false;
            }
        }
        return false;
    }

    public Map<String, String> getVideoIdentifyMap(TaskDetailInfoDto task) {
        VideoContainer videoContainer = videoContainerMap.get(task.getTaskCameraPrefix());
        if (videoContainer == null){
            return null;
        }
        return videoContainer.getIdentifyMap();
    }

    public VideoContainer getVideoContainerByType(TaskDetailInfoDto task, TaskDetailInfoDto.TaskType taskType) {

        TaskDetailInfoDto.TaskType originalTaskType = task.getTaskType();
        task.setTaskType(taskType);
        VideoContainer videoContainer = videoContainerMap.get(task.getTaskCameraPrefix());
        task.setTaskType(originalTaskType);
        return videoContainer;
    }


    public boolean addBodyColleAndFaceColleRelation(TaskDetailInfoDto task, String bodyColleKey, String faceColleKey){
        VideoContainer faceVideoContainer = getVideoContainerByType(task, TaskDetailInfoDto.TaskType.FACE);
        VideoContainer bodyVideoContainer = getVideoContainerByType(task, TaskDetailInfoDto.TaskType.BODY);

        Map<String, Set<String>> relationMap = bodyVideoContainer.getBodyColleAndFaceColleRelationMap();
        Set<String> faceColleKeySet = relationMap.get(bodyColleKey);
        if (faceColleKeySet == null){
            Set<String> set = Collections.synchronizedSet(new HashSet<String>());
            set.add(faceColleKey);
            relationMap.put(bodyColleKey, set);
        }else {
            faceColleKeySet.add(faceColleKey);
        }
        return true;
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
