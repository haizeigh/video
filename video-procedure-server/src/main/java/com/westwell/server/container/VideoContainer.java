package com.westwell.server.container;

import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.dto.TaskDetailInfoDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@Data
public class VideoContainer {

//    身份信息
    private Map<String, String> identifyMap = new ConcurrentHashMap<>(DataConfig.CLUSTER_NUM * 2);
//    临时底库
    private List<String> picCollection = new CopyOnWriteArrayList<>();
//    堆栈限制计数
    private AtomicInteger clusterCount = new AtomicInteger(0);
    //    全部的帧
    private  List<String> allFrame = new CopyOnWriteArrayList<>();

//    具有对应关系的 bodyColle -> FaceColle
    private Map<String, Set<String>> bodyColleAndFaceColleRelationMap = new ConcurrentHashMap<>(DataConfig.CLUSTER_NUM * 2);

    @Resource
    private ApplicationContext applicationContext;


    public String newBucketName(TaskDetailInfoDto task){
        if (!hasNewBucket(task)){
            return null;
        }

        return new StringBuilder(task.getTaskCameraPrefix())
                .append(":set:")
                .append(clusterCount.incrementAndGet()).toString();
    }


    public boolean hasNewBucket(TaskDetailInfoDto task) {

        if (clusterCount.get() > DataConfig.CLUSTER_NUM) {
            log.warn("clusterCount={} 超出限制{}", clusterCount, DataConfig.CLUSTER_NUM);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new TreeSet<>();

        String a1 = new String("a");
        String a2 = new String("a2");
        set1.add(a1);
        set1.add(a2);
        System.out.println(set1);

        String b1 = new String("b");
        String b2 = new String("b2");
        set2.add(b1);
        set2.add(b2);
        System.out.println(set2);
    }
}
