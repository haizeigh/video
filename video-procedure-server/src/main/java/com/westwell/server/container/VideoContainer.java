package com.westwell.server.container;

import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.dto.TaskDetailInfoDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@Data
public class VideoContainer {

//    身份信息
    private Map<String, String> identifyMap = new ConcurrentHashMap<>(DataConfig.CLUSTER_NUM * 2);
//    底酷
    private List<String> picCollection = new CopyOnWriteArrayList<>();
//    堆栈限制计数
    private AtomicInteger clusterCount = new AtomicInteger(0);
    //    全部的帧
    private  List<String> allFrame = new CopyOnWriteArrayList<>();

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
}
