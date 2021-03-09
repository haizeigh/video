package com.westwell.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.westwell.api.common.utils.DateUtils;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.ExportUtil;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainerManager;
import com.westwell.server.container.VideoContainer;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.dto.TaskSummaryResultDto;
import com.westwell.server.service.ResultDumpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class ResultDumpServiceImpl implements ResultDumpService {

    @Resource
    RedisUtils redisUtils;

    @Resource
    IdentifyContainerManager identifyContainerManager;

    @Override
    public void dumpFrameResult(TaskDetailInfoDto task, String textPath) throws Exception {

        List<String> sortedPicKeys = identifyContainerManager.getSortedPicKeys(task);
        if (CollectionUtils.isEmpty(sortedPicKeys)){
            log.info("任务no={}没有帧数据",task.getTaskEntity().getTaskNo() );
            return;
        }
        String taskNo = task.getTaskEntity().getTaskNo().toString();
        String cameraNo = task.getTaskEntity().getCameraNo().toString();
        String taskTime = getFormatTaskTime(task.getTaskEntity().getTaskStartTime());
        AtomicInteger ato = new AtomicInteger(0);
//        String mapKey = "task_no,camera_no,pic_time,frame_no,location,pic,feature,student_id,student_name";
        String mapKey = "no,task_no,camera_no,pic_time,frame_no,location,student_id,student_name,pic_key,create_time";
        List<Map<String, String>> dataList = new ArrayList<>();
        List<String> queryList = new ArrayList<>();
        queryList.add(DataConfig.LOCATION);
        queryList.add(DataConfig.STUDENT_ID);

        for (String picKey : sortedPicKeys) {

            List<Object> queryListObjects = redisUtils.multiGetHash(picKey, queryList);
            String location = queryListObjects.get(0).toString();
            String studentId = queryListObjects.get(1) == null ? null : queryListObjects.get(1).toString() ;
            if (Strings.isNullOrEmpty(studentId)){
                //没有推理结果的不存储
                continue;
            }

            Map<String, String> map = new HashedMap();
            String[] split = picKey.split(":");

            map.put("no", taskNo + DataConfig.DASH +ato.getAndIncrement());
            map.put("task_no", taskNo);
            map.put("camera_no", cameraNo);
            map.put("pic_time", split[3]);
            map.put("frame_no", split[4]);

            map.put("location", location);
            map.put("student_id", studentId);
            map.put("student_name", "");

            map.put("pic_key", picKey);
            map.put("create_time", taskTime);
            dataList.add(map);
        }

        File file = new File(textPath);
        if (!file.exists()){
            file.mkdirs();
        }

        try {
            OutputStream outputStream = new FileOutputStream(textPath+ "/" + "frame.csv");
            ExportUtil.doExport(dataList, mapKey, outputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void dumpTaskTemptResult(TaskDetailInfoDto task, String textPath) {

        Map<String, String> identifyMap = identifyContainerManager.getVideoIdentifyMap(task);
        if (MapUtils.isEmpty(identifyMap)){
            log.info("任务no={}没有推理数据, 无法导出特征临时底库",task.getTaskEntity().getTaskNo() );
            return;
        }
        VideoContainer videoContainer = identifyContainerManager.getVideoContainer(task);

        String taskNo = task.getTaskEntity().getTaskNo().toString();
        String cameraNo = task.getTaskEntity().getCameraNo().toString();
        AtomicInteger ato = new AtomicInteger(0);
        String taskTime = getFormatTaskTime(task.getTaskEntity().getTaskStartTime());

        String mapKey = "no,task_no,camera_no,pic_time,frame_no,location,student_id,student_name,pic_key,pic_path,create_time";

        Map<String, List<String>> studentyMap = new HashedMap();
//        根据学生分组
        identifyMap.forEach( ( picColleKey, studentId ) -> {

            List<String> colleKeyList = studentyMap.get(studentId);
            if (CollectionUtils.isEmpty(colleKeyList)){
                colleKeyList = new ArrayList<>();

                //去取特征集合
                colleKeyList.add(videoContainer.getSpecialPicColleKey(picColleKey));
                studentyMap.put(studentId, colleKeyList);
                return;
            }
            colleKeyList.add(picColleKey);

        } );
        List<Map<String, String>> dataList = new ArrayList<>();

        studentyMap.forEach( ( studentId, colleKeyList ) -> {
            //            遍历所有人的特征底库
            colleKeyList.stream().forEach(picColleKey -> {

                List<String> picKeys = identifyContainerManager.getPicsFromBucket(picColleKey);
                if (CollectionUtils.isEmpty(picKeys)){
                    return;
                }
//            遍历所有小图
                picKeys.forEach(picKey -> {

                    String location = redisUtils.getHash(picKey, DataConfig.LOCATION).toString();
                    Map<String, String> map = new HashedMap();

                    String[] split = picKey.split(":");

                    map.put("no", taskNo + DataConfig.DASH +ato.getAndIncrement());
                    map.put("task_no", taskNo);
                    map.put("camera_no", cameraNo);
                    map.put("pic_time", split[3]);
                    map.put("frame_no", split[4]);

                    map.put("location", location);
                    map.put("pic_path", task.getTaskTemptPathForLabelCollection() + "/" + studentId);

                    map.put("student_id", studentId);
                    map.put("student_name", "");
                    map.put("pic_key", picKey);
                    map.put("create_time", taskTime );

                    dataList.add(map);
                });
            } );
        });

        File file = new File(textPath);
        if (!file.exists()){
            file.mkdirs();
        }

        try {
            OutputStream outputStream = new FileOutputStream(textPath+ "/" + "tempColle.csv");
            ExportUtil.doExport(dataList, mapKey, outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void dumpTaskSummaryResult(TaskDetailInfoDto task, String textPath) throws Exception {

        Map<String, String> identifyMap = identifyContainerManager.getVideoIdentifyMap(task);
        if (MapUtils.isEmpty(identifyMap)){
            log.info("任务no={}没有推理数据",task.getTaskEntity().getTaskNo() );
            return;
        }

        Integer taskNo = task.getTaskEntity().getTaskNo();
        Integer cameraNo = task.getTaskEntity().getCameraNo();

        String videoTime = DateUtils.format(task.getTaskEntity().getVideoStartTime());
        String taskTime = getFormatTaskTime(task.getTaskEntity().getTaskStartTime());
        String collePath = task.getTaskTemptPathForLabelCollection();

        AtomicInteger summaryAto = new AtomicInteger(0);
        AtomicInteger picAto = new AtomicInteger(0);

        List<TaskSummaryResultDto> dataList = new ArrayList<>();
        List<Map<String, String>> frameDataList = new ArrayList<>();
        String summaryMapKey = "no,task_no,camera_no,student_id,student_name,start_time,end_time,video_day,create_time";
        String picMapKey = "no,summary_no,task_no,camera_no,pic_time,frame_no,location,student_id,student_name,pic_key,pic_path,create_time";

        Map<String, List<String>> studentyMap = new HashedMap();
//        根据学生分组
        identifyMap.forEach( ( picColleKey, studentId ) -> {

            List<String> colleKeyList = studentyMap.get(studentId);
            if (CollectionUtils.isEmpty(colleKeyList)){
                colleKeyList = new ArrayList<>();
                colleKeyList.add(picColleKey);
                studentyMap.put(studentId, colleKeyList);
                return;
            }
            colleKeyList.add(picColleKey);

        } );

        studentyMap.forEach( ( studentId, colleKeyList ) -> {
            //            遍历所有人
            colleKeyList.stream().forEach(picColleKey -> {

                List<String> picKeys = identifyContainerManager.getPicsFromBucket(picColleKey);
                Collections.sort(picKeys);

//            遍历所有小图
                picKeys.forEach(picKey -> {

                    String[] split = picKey.split(":");
                    String location = redisUtils.getHash(picKey, "location").toString();
                    String thisStudentId = studentId;
                    long picTime = Long.parseLong(split[3]);
                    int frameNum = Integer.parseInt(split[4]);

//                初始化
                    if (CollectionUtils.isEmpty(dataList)){
                        TaskSummaryResultDto result = createResult(summaryAto, taskNo, cameraNo, thisStudentId, "", picTime, videoTime, taskTime);
                        dataList.add(result);

                        insertSummaryPicMap(picAto, result, split[3], split[4], location,
                                thisStudentId, "", collePath, picKey, taskTime, frameDataList);
                        return;
                    }

                    TaskSummaryResultDto lastTaskSummaryResultDto = dataList.get(dataList.size() - 1);

//               和前一个对比 整合
                    if (
                            lastTaskSummaryResultDto.getStudent_id().equals(thisStudentId)
                            && picTime - lastTaskSummaryResultDto.getEnd_time() > 0
                    ){
                        lastTaskSummaryResultDto.setEnd_time(picTime);
                    }else if (
                            lastTaskSummaryResultDto.getStudent_id().equals(thisStudentId)
                                    && picTime - lastTaskSummaryResultDto.getEnd_time() <= 0
                    ){
                        log.info("同一个人的位置信息，时间不能后退,不再整合图片:{}", picKey);
                        return;
                    }else {
//                    新插入数据
                        TaskSummaryResultDto result = createResult(summaryAto, taskNo, cameraNo, thisStudentId, "", picTime, videoTime, taskTime);
                        dataList.add(result);
                        lastTaskSummaryResultDto = result;
                    }

                    insertSummaryPicMap(picAto, lastTaskSummaryResultDto, split[3], split[4], location,
                            thisStudentId, "", collePath, picKey, taskTime, frameDataList);
                });
            } );
        });


        File file = new File(textPath);
        if (!file.exists()){
            file.mkdirs();
        }

        List<Map<String, String>> collect = new ArrayList<>();
        for (TaskSummaryResultDto taskSummaryResultDto : dataList) {
            Map<String, String> describe = BeanUtils.describe(taskSummaryResultDto);
//            describe.put("locations", JSON.toJSONString(taskSummaryResultDto.getLocations()));
            collect.add(describe);
        }

        OutputStream summaryOutputStream = new FileOutputStream(textPath+ "/" + "summary.csv");
        ExportUtil.doExport(collect, summaryMapKey, summaryOutputStream);

        OutputStream frameOutputStream = new FileOutputStream(textPath+ "/" + "summaryFrame.csv");
        ExportUtil.doExport(frameDataList, picMapKey, frameOutputStream);


    }

    private String getFormatTaskTime(Date taskStartTime) {
        return DateUtils.format(taskStartTime, DateUtils.DATE_TIME_PATTERN);
    }

    private void insertSummaryPicMap(AtomicInteger ato, TaskSummaryResultDto taskSummaryResultDto, String picTime,
                                     String frameNo , String location, String studentId, String studentName, String collePath, String picKey,
                                     String createTime, List<Map<String, String>> frameDataList){
//        String picMapKey = "no,summary_no,task_no,camera_no,pic_time,frame_no,location,student_id,student_name,pic_key,pic_path,create_time";
        Map<String, String> map = new HashedMap();
        map.put("no", taskSummaryResultDto.getTask_no() + DataConfig.DASH +ato.getAndIncrement());
        map.put("task_no", taskSummaryResultDto.getTask_no());

        map.put("summary_no", taskSummaryResultDto.getNo());
        map.put("camera_no", taskSummaryResultDto.getCamera_no());
        map.put("pic_time", picTime );
        map.put("frame_no", frameNo);

        map.put("location", location);
        map.put("student_id", studentId);
        map.put("student_name", studentName);

        map.put("pic_key", picKey);
        map.put("pic_path", collePath+ "/" +studentId);
        map.put("create_time", createTime);

        frameDataList.add(map);
    }

    private TaskSummaryResultDto createResult(AtomicInteger ato, Integer taskNo, Integer cameraNo,
                                              String thisStudentId, String thisStudentName, long picTime, String videoDay, String createTime) {
        TaskSummaryResultDto taskSummaryResultDto = TaskSummaryResultDto.builder()
                .no(taskNo.toString()+ DataConfig.DASH +ato.getAndIncrement())
                .task_no(taskNo.toString())
                .camera_no(cameraNo.toString())

                .student_id(thisStudentId)
                .student_name(thisStudentName)
                .start_time(picTime)
                .end_time(picTime)

                .video_day(videoDay)
                .create_time(createTime)
                .build();
        return taskSummaryResultDto;
    }

    public static void main(String[] args) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        TaskSummaryResultDto taskSummaryResultDto = new TaskSummaryResultDto();
        List<String> locations = new ArrayList<>();
        locations.add("location1");
        locations.add("location2");
//        taskSummaryResultDto.setLocations(locations);

        Map<String, String> describe = BeanUtils.describe(taskSummaryResultDto);
//        describe.put("locations", JSON.toJSONString(taskSummaryResultDto.getLocations()));

        System.out.println(JSON.toJSONString(describe));

        String pic = "1614051005000";
        System.out.println(Long.parseLong(pic));
    }

}

