package com.westwell.server.container;

import com.westwell.server.common.configs.DataConfig;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@Data
public class IdentifyFacesContainer {

    private   Map<String, String> identifyMap = new ConcurrentHashMap<>( DataConfig.CLUSTER_NUM * 2 );
    private  List<String> faceCollection= new CopyOnWriteArrayList<>();
    private  AtomicInteger clusterCount = new AtomicInteger(0);
    //    全部的帧
    private  List<String> allFrame = new LinkedList<>();

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
        allFrame = new LinkedList<>();
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

        StringBuilder stringBuilder = new StringBuilder();
        String faceColleKey = stringBuilder.append(DataConfig.TASK_PREFIX)
                .append(":")
                .append(task.getTaskEntity().getTaskNo())
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

    public  List<String> faceColleKeys() {
        return faceCollection;
    }

    public  boolean containsKey(String faceColleKey) {
        return faceCollection.indexOf(faceColleKey) > 0;
    }

//    查找所有排序的帧
    public  List<String> getSortedFaceKeys(){

        Collections.sort(allFrame);
        return allFrame;
    }

    public  void addFrameFaceKeys(List<String> faceKeys){
        allFrame.addAll(faceKeys);
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
