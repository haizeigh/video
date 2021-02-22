package com.westwell.server.container;

import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.event.FaceCollectionChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class IdentifyFacesContainer {

    public static Map<String, String> IDENTIFY_MAP = new ConcurrentHashMap<>( DataConfig.CLUSTER_NUM * 2 );
    public static Map<String, List<String>> FACE_COLLECTION_MAP = new ConcurrentHashMap(DataConfig.CLUSTER_NUM * 2 );
    public static AtomicInteger CLUSTER_COUNT = new AtomicInteger(0);

    private static ApplicationContext applicationContext;

//    全部的帧
    private static List<String> ALL_FRAME = new LinkedList<>();



    public IdentifyFacesContainer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static void addPicToExistBucket(String faceKey, String faceColleKey){

        new FaceCollectionChangeEvent("change", faceColleKey);
        FACE_COLLECTION_MAP.get(faceColleKey).add(faceKey);

    }

    public static boolean addPicToNewBucket(String faceKey, TaskDetailInfoDto task) {

        if (CLUSTER_COUNT.get() > DataConfig.CLUSTER_NUM) {
            log.warn("CLUSTER_COUNT={} 超出限制{}", CLUSTER_COUNT, DataConfig.CLUSTER_NUM);
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(DataConfig.TASK_PREFIX)
                .append(":")
                .append(task.getTaskEntity().getTaskNo())
                .append(":set:")
                .append(CLUSTER_COUNT.incrementAndGet());

        List<String> newFaceCollePics = new ArrayList<>();
        newFaceCollePics.add(faceKey);
        FACE_COLLECTION_MAP.put(stringBuilder.toString(), newFaceCollePics);

        FaceCollectionChangeEvent change = new FaceCollectionChangeEvent("change", stringBuilder.toString());
        applicationContext.publishEvent(change);
        return true;
    }

//    查找身份
    public static String getIdentify(String colleKey){
        return IDENTIFY_MAP.get(colleKey);
    }

//    写入确定的身份
    public static void addIdentify(String colleKey, String studentId){
        IDENTIFY_MAP.put(colleKey, studentId);
    }

    public static Iterable<String> getKeySet() {
        return FACE_COLLECTION_MAP.keySet();
    }

    public static boolean containsKey(String faceColleKey) {
        return FACE_COLLECTION_MAP.containsKey(faceColleKey);
    }

//    查找所有排序的帧
    public static List<String> getSortedFaceKeys(){

        Collections.sort(ALL_FRAME);
        return ALL_FRAME;
    }

    public static void addFrameFaceKeys(List<String> faceKeys){
        ALL_FRAME.addAll(faceKeys);
    }


    public static void main(String[] args) {
        List<String> sortedFaceKeys = new ArrayList<>();
        sortedFaceKeys.add("b");
        sortedFaceKeys.add("a");
        System.out.println(sortedFaceKeys);
        Collections.sort(sortedFaceKeys);
        System.out.println(sortedFaceKeys);
    }

}
