package com.westwell.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ProtocolStringList;
import com.westwell.api.*;
import com.westwell.api.wellcare.body.BodyFeatureServiceGrpc;
import com.westwell.api.wellcare.body.BodyPicsInRedisRequest;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.common.utils.RedisUtils;
import com.westwell.server.container.IdentifyContainerManager;
import com.westwell.server.dto.CompareSimilarityDto;
import com.westwell.server.dto.TaskDetailInfoDto;
import com.westwell.server.service.FeatureService;
import com.westwell.server.service.VideoMediaService;
import com.westwell.server.service.base.RpcBaseInspectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
@Service
public class FeatureServiceImpl extends RpcBaseInspectService implements FeatureService {

    @Resource
    FeatureServiceGrpc.FeatureServiceBlockingStub featureServiceBlockingStub;

    @Resource
    BodyFeatureServiceGrpc.BodyFeatureServiceBlockingStub bodyFeatureServiceBlockingStub;


    @Resource
    IdentifyContainerManager identifyContainerManager;

    @Resource
    RedisUtils redisUtils;

    @Resource
    VideoMediaService videoMediaService;

    @Override
    public boolean extractFaceFeature(List<String> picKeys) {

        log.info("提取face特征的图片数目{}", picKeys.size());
        FacePicsInRedisRequest facePicsInRedisRequest = FacePicsInRedisRequest.newBuilder().addAllPicKeys(picKeys).build();
        ExtractFeatureInRedisResponse extractFeatureInRedisResponse = featureServiceBlockingStub.extractFeatureInRedis(facePicsInRedisRequest);
        return extractFeatureInRedisResponse.getSuccess().equals(DataConfig.SUCCESS);
    }

    @Override
    public boolean extractBodyFeature(List<String> picKeys) {
        log.info("提取body特征的图片数目{}, 第一个{}", picKeys.size(), picKeys.get(0));

        BodyPicsInRedisRequest.Builder builder = BodyPicsInRedisRequest.newBuilder();
        builder.addAllPicKeys(picKeys);

        com.westwell.api.wellcare.body.ExtractFeatureInRedisResponse extractFeatureInRedisResponse = bodyFeatureServiceBlockingStub.extractBodyFeatureInRedis(builder.build());
        return extractFeatureInRedisResponse.getSuccess().equals(DataConfig.SUCCESS);

    }

    @Override
    public CompareSimilarityDto compareFaceWithCollection(TaskDetailInfoDto task, String faceKey) throws Exception {

        List<String> picCollesList = identifyContainerManager.picColleKeys(task);
        ContrastPicWithCollesRequest build = ContrastPicWithCollesRequest.newBuilder()
                .setPicKey(faceKey)
                .addAllPicColles(picCollesList)
                .build();
//        log.info("特征比对{}， 集合{}", faceKey, picCollesList);

        ContrastPicWithCollesResponse contrastPicWithCollesResponse = featureServiceBlockingStub.contrastPicWithCollesInRedis(build);
        int code = contrastPicWithCollesResponse.getCode();
        String msg = contrastPicWithCollesResponse.getMsg();
        checkInterface("face对比1：N", code, msg);

        List<Double> similarityListForQuery = contrastPicWithCollesResponse.getSimilarityOrderedListList();
        log.info("特征比对{}， 集合{}, 结果{}", faceKey, picCollesList, similarityListForQuery);
        deugCompare(task, faceKey, picCollesList, similarityListForQuery);

        return compareSimilarity(picCollesList, similarityListForQuery);
    }

    private void deugCompare(TaskDetailInfoDto task, String faceKey, List<String> picCollesList, List<Double> similarityListForQuery) throws Exception {
        if (DataConfig.DEBUG_FLAG){
            String debugPath = task.getCompareColleDebugPath() + "/" + System.currentTimeMillis();
            videoMediaService.readPicFromRedis(faceKey, debugPath);
            for (int i = 0; i < similarityListForQuery.size(); i++) {
                Double score = similarityListForQuery.get(i);
                String picColle = picCollesList.get(i);
                List<String> picsFromBucket = identifyContainerManager.getPicsFromBucket(picColle);
                videoMediaService.readPicsFromRedis(task, picsFromBucket, debugPath +"/" + score );
            }

        }
    }


    @Override
    public CompareSimilarityDto compareBodyWithCollection(TaskDetailInfoDto task, String bodyKey) throws Exception {

        List<String> picCollesList = identifyContainerManager.picColleKeys(task);
        com.westwell.api.wellcare.body.ContrastPicWithCollesRequest build = com.westwell.api.wellcare.body.ContrastPicWithCollesRequest.newBuilder()
                .setPicKey(bodyKey)
                .addAllPicColles(picCollesList)
                .build();
//        log.info("特征比对{}， 集合{}", faceKey, picCollesList);

        com.westwell.api.wellcare.body.ContrastPicWithCollesResponse contrastPicWithCollesResponse = bodyFeatureServiceBlockingStub.comparePicBodyWithCollesInRedis(build);
        int code = contrastPicWithCollesResponse.getCode();
        String msg = contrastPicWithCollesResponse.getMsg();
        checkInterface("body对比1：N", code, msg);


        List<Double> similarityListForQuery = contrastPicWithCollesResponse.getSimilarityOrderedListList();
        log.info("特征比对{}， 集合{}, 结果{}", bodyKey, picCollesList, similarityListForQuery);
        deugCompare(task, bodyKey, picCollesList, similarityListForQuery);

        return compareSimilarity(picCollesList, similarityListForQuery);
    }

    @Override
    public CompareSimilarityDto comparePicWithCollection(TaskDetailInfoDto task, String picKey) throws Exception {

        if (TaskDetailInfoDto.TaskType.FACE == task.getTaskType() ){
            return compareFaceWithCollection(task, picKey);
        }else if ( TaskDetailInfoDto.TaskType.BODY == task.getTaskType() ){
            return compareBodyWithCollection(task, picKey);
        }else {
            return null;
        }

    }

    @Override
    public String compareCollectionWithStudent(TaskDetailInfoDto task, String faceColle) throws Exception {

        ArrayList<String> faceColleList = new ArrayList<>();
        faceColleList.add(faceColle);

        ContrastCollesWithBaseInfoRequest build = ContrastCollesWithBaseInfoRequest.newBuilder()
                .addAllPicColles(faceColleList)
                .build();
        ContrastCollesWithBaseInfoResponse contrastCollesWithBaseInfoResponse =
                featureServiceBlockingStub.contrastCollesWithBaseInfoInRedis(build);
        int code = contrastCollesWithBaseInfoResponse.getCode();
        String msg = contrastCollesWithBaseInfoResponse.getMsg();
        checkInterface("face对比N：M", code, msg);


        ProtocolStringList colleWithStudentResultsList = contrastCollesWithBaseInfoResponse.getColleWithStudentResultsList();
        log.info("集合{}，对比基础库的结果{}", faceColle, colleWithStudentResultsList);
        if (CollectionUtils.isEmpty(colleWithStudentResultsList)){
            return null;
        }

        String result = colleWithStudentResultsList.get(0);
        String[] split = result.split("\\|");
        double similarity = Double.parseDouble(split[2]);
        String colleKey = split[1];

        if (DataConfig.DEBUG_FLAG){
            String debugPath = task.getCompareStudentDebugPath() + "/" + result;
            List<String> picsFromBucket = identifyContainerManager.getPicsFromBucket(faceColle);
            videoMediaService.readPicsFromRedis(task, picsFromBucket, debugPath);
        }

        if (similarity < DataConfig.STUDENT_SIMILARITY){
//            不达到阈值
            return null;
        }

        return colleKey;
    }



    private CompareSimilarityDto compareSimilarity(List<String> picCollesList, List<Double> similarityListForQuery) {
        if (CollectionUtils.isEmpty(similarityListForQuery)){
//            没有结果
            return null;
        }
        List<Double> similarityOrderedListList = new ArrayList<>(similarityListForQuery);
        Collections.sort(similarityOrderedListList);

        CompareSimilarityDto compareSimilarityDto = new CompareSimilarityDto();

//        存在大于阈值的情况
        Optional<Double> firstOverSimilarity = similarityOrderedListList
                .stream()
                .filter(similarity -> similarity > DataConfig.MAX_SIMILARITY )
                .findFirst();
        if (firstOverSimilarity.isPresent()){
            log.info("too much similarity");
//            太似性 返回最相似
            compareSimilarityDto.setOverSimilarity(true);

            Double most = firstOverSimilarity.get();
            int mostIndex = similarityListForQuery.indexOf(most);
            String mostPicColle = picCollesList.get(mostIndex);

            compareSimilarityDto.setSimilarity(most);
            compareSimilarityDto.setPicColleKey(mostPicColle);
            return compareSimilarityDto;
        }


//        比一下最大一个
        if (similarityOrderedListList.get( similarityOrderedListList.size() -1 ) < DataConfig.MIN_SIMILARITY){
//            都不相似，单独的结果 不用比较
            log.info("very less similarity");
            return compareSimilarityDto;
        }

//        找到相似的目标
        Optional<Double> first = similarityOrderedListList
                .stream()
                .filter(similarity -> similarity <= DataConfig.MAX_SIMILARITY && similarity >= DataConfig.MIN_SIMILARITY)
                .findFirst();
        Double rightNum = first.get();


        int rightNumIndex = similarityListForQuery.indexOf(rightNum);
        String rightNumPicColle = picCollesList.get(rightNumIndex);
        log.info("rightNumIndex=" + rightNumIndex);

        compareSimilarityDto.setSimilarity(rightNum);
        compareSimilarityDto.setPicColleKey( rightNumPicColle );
        return compareSimilarityDto;
    }



    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        List<Double> similarityOrderedListList = new ArrayList<>();
        similarityOrderedListList.add(0.9);
        similarityOrderedListList.add(0.8);

        List<String> picCollesList = new ArrayList<>();
        picCollesList.add("S1");
        picCollesList.add("S2");

        int size = similarityOrderedListList.size();
        Map<Double, String>  picCollesSimiMap = new HashedMap( size * 2 );
        for (int i = 0; i < size; i++){
            picCollesSimiMap.put(similarityOrderedListList.get(i), picCollesList.get(i));
        }
        System.out.println(JSON.toJSONString(picCollesSimiMap));


        List<Double> list = new ArrayList<>(similarityOrderedListList);
        Collections.sort(list);

        System.out.println(list);
        System.out.println(similarityOrderedListList);

    }
}
