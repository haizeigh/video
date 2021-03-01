package com.westwell.server.container;

import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.event.FaceCollectionChangeEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@Data
public class IdentifyFacesContainer3 {

    private   Map<String, String> identifyMap = new ConcurrentHashMap<>( DataConfig.CLUSTER_NUM * 2 );
    private  List<String> faceCollection= new CopyOnWriteArrayList<>();
    private  AtomicInteger clusterCount = new AtomicInteger(0);
    //    全部的帧
    private  Map<String, List<String>> allFrameMap = new ConcurrentHashMap<>( DataConfig.CLUSTER_NUM * 2 );
//    private  List<String> allFrame = new CopyOnWriteArrayList<>();

    @Resource
    private  ApplicationContext applicationContext;

    @Resource
    private RedisUtils redisUtils;

/*
    public IdentifyFacesContainer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

*/

    public void init(){
        identifyMap = new ConcurrentHashMap<>( DataConfig.CLUSTER_NUM * 2 );
        faceCollection= new CopyOnWriteArrayList<>();
        clusterCount = new AtomicInteger(0);
        allFrameMap = new ConcurrentHashMap<>( DataConfig.CLUSTER_NUM * 2 );
    }


    public  void addPicToExistBucket(String faceKey, String faceColleKey){

        redisUtils.lPush(faceColleKey, faceKey);
        FaceCollectionChangeEvent change = new FaceCollectionChangeEvent("change", faceColleKey);
        applicationContext.publishEvent(change);
    }

    public  boolean addPicToNewBucket(String faceKey, TaskDetailInfoDto task) {

        if (clusterCount.get() > DataConfig.CLUSTER_NUM) {
            log.warn("clusterCount={} 超出限制{}", clusterCount, DataConfig.CLUSTER_NUM);
            return false;
        }

//        同一个人物的分叉问题
        StringBuilder stringBuilder = new StringBuilder();
        String faceColleKey = stringBuilder.append(DataConfig.TASK_PREFIX)
                .append(":")
                .append(task.getTaskEntity().getTaskNo())
                .append(":")
                .append(task.getTaskEntity().getCameraNo())
                .append(":set:")
                .append(clusterCount.incrementAndGet()).toString();

        faceCollection.add(faceColleKey);
        addPicToExistBucket(faceKey, faceColleKey);

        return true;
    }

    public  List<String> getPicsFromBucket(String faceColleKey){
        return redisUtils.lGetAll(faceColleKey).stream().map( v -> v.toString()).collect(Collectors.toList());
    }

//    查找身份
    public  String getIdentify(String colleKey){
        return identifyMap.get(colleKey);
    }

//    写入确定的身份
    public  void addIdentify(String colleKey, String studentId){
        identifyMap.put(colleKey, studentId);
    }

    public  List<String> faceColleKeys(TaskDetailInfoDto task) {

        return faceCollection;
    }

    public  boolean containsKey(String faceColleKey) {
        return faceCollection.indexOf(faceColleKey) > 0;
    }

//    查找所有排序的帧
    public  List<String> getSortedFaceKeys(TaskDetailInfoDto task){

        String taskCamera = getTaskCamera(task);
        List<String> faces = allFrameMap.get(taskCamera);
        Collections.sort(faces);
        return faces;
    }

    public  void addFrameFaceKeys(TaskDetailInfoDto task, List<String> faceKeys){

        String taskCamera = getTaskCamera(task);

        if (allFrameMap.get(taskCamera) != null){
            allFrameMap.get(taskCamera).addAll(faceKeys);
        }else {
            allFrameMap.put(taskCamera, faceKeys);
        }

    }

    private String getTaskCamera(TaskDetailInfoDto task) {
        StringBuilder stringBuilder = new StringBuilder();
        String taskCamera = stringBuilder.append(DataConfig.TASK_PREFIX)
                .append(":")
                .append(task.getTaskEntity().getTaskNo())
                .append(":")
                .append(task.getTaskEntity().getCameraNo()).toString();
        return taskCamera;
    }

    public void initBucket(List<String> faceKeys, TaskDetailInfoDto task) {

        if (CollectionUtils.isEmpty(faceCollection)){
            synchronized (this){
                if (CollectionUtils.isEmpty(faceCollection)){
                    addPicToNewBucket(faceKeys.get(0), task);

                    List<String> newFaceKeys = new ArrayList<>();
                    for (int i = 1; i < faceKeys.size(); i++) {
                        newFaceKeys.add(faceKeys.get(i));
                    }
                    faceKeys = newFaceKeys;
                }
            }
        }
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
