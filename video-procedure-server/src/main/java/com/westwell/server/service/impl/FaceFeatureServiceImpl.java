package com.westwell.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.westwell.api.ContrastCollesWithBaseInfoRequest;
import com.westwell.api.ContrastCollesWithBaseInfoResponse;
import com.westwell.api.ContrastPicWithCollesRequest;
import com.westwell.api.FeatureServiceGrpc;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.container.IdentifyFacesContainer;
import com.westwell.server.dto.CompareSimilarityDto;
import com.westwell.server.service.FaceFeatureService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class FaceFeatureServiceImpl implements FaceFeatureService {

    @Resource
    FeatureServiceGrpc.FeatureServiceBlockingStub featureServiceBlockingStub;

    @Resource
    IdentifyFacesContainer identifyFacesContainer;

    @Override
    public boolean extractFaceFeature(List<String> picKeys) {

/*        FacePicsInRedisRequest facePicsInRedisRequest = FacePicsInRedisRequest.newBuilder().addAllPicKeys(picKeys).build();
        ExtractFeatureInRedisResponse extractFeatureInRedisResponse = featureServiceBlockingStub.extractFeatureInRedis(facePicsInRedisRequest);
        return extractFeatureInRedisResponse.getSuccess().equals(DataConfig.SUCCESS);*/
        return true;
    }

    @Override
    public CompareSimilarityDto compareFaceWithCollection(String faceKey) throws Exception {

        List<String> picCollesList = identifyFacesContainer.faceColleKeys();
        ContrastPicWithCollesRequest build = ContrastPicWithCollesRequest.newBuilder()
                .setPicKey(faceKey)
                .addAllPicColles(picCollesList)
                .build();

 /*       ContrastPicWithCollesResponse contrastPicWithCollesResponse = featureServiceBlockingStub.contrastPicWithCollesInRedis(build);
        List<Double> similarityListForQuery = contrastPicWithCollesResponse.getSimilarityOrderedListList();*/


        List<Double> similarityListForQuery = new ArrayList<>();
        picCollesList.forEach(picColle -> similarityListForQuery.add(Math.random()));

        if (CollectionUtils.isEmpty(similarityListForQuery)){
//            没有结果
            return null;
        }
        List<Double> similarityOrderedListList = new ArrayList<>(similarityListForQuery);

        CompareSimilarityDto compareSimilarityDto = new CompareSimilarityDto();
        if (similarityOrderedListList.get(similarityOrderedListList.size() -1) > DataConfig.MAX_SIMILARITY){
//            太似性 返回最相似
            compareSimilarityDto.setOverSimilarity(true);

            Double most = similarityOrderedListList.get(0);
            int mostIndex = similarityListForQuery.indexOf(most);
            String mostPicColle = picCollesList.get(mostIndex);

            compareSimilarityDto.setSimilarity(most);
            compareSimilarityDto.setPicColleKey(mostPicColle);
            return compareSimilarityDto;
        }

        if (similarityOrderedListList.get(0) < DataConfig.MIN_SIMILARITY){
//            都不相似，单独的结果 不用比较
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


        compareSimilarityDto.setSimilarity(rightNum);
        compareSimilarityDto.setPicColleKey( rightNumPicColle );
        return compareSimilarityDto;
    }

    @Override
    public Map<String, String> compareCollectionWithStudent(List<String> faceColles) {

        Map<String, String> map = new HashedMap();
        map.put(faceColles.get(0), "1001");
        return map;
    }

    @Override
    public String compareCollectionWithStudent(String faceColle) {

        ArrayList<String> faceColleList = new ArrayList<>();
        faceColleList.add(faceColle);

        ContrastCollesWithBaseInfoRequest build = ContrastCollesWithBaseInfoRequest.newBuilder()
                .addAllPicColles(faceColleList)
                .build();
        ContrastCollesWithBaseInfoResponse contrastCollesWithBaseInfoResponse =
                featureServiceBlockingStub.contrastCollesWithBaseInfoInRedis(build);
        return contrastCollesWithBaseInfoResponse.getColleWithStudentResults(0).getStudentNum();
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
