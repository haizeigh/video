package com.westwell.server.service.impl;

import com.google.protobuf.ProtocolStringList;
import com.westwell.api.*;
import com.westwell.server.common.configs.DataConfig;
import com.westwell.server.container.IdentifyFacesContainer;
import com.westwell.server.dto.CompareSimilarityDto;
import com.westwell.server.service.FaceFeatureService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FaceFeatureServiceImpl implements FaceFeatureService {

    @Resource
    FeatureServiceGrpc.FeatureServiceBlockingStub featureServiceBlockingStub;


    @Override
    public boolean extractFaceFeature(List<String> picKeys) {

        FacePicsInRedisRequest facePicsInRedisRequest = FacePicsInRedisRequest.newBuilder().addAllPicKeys(picKeys).build();
        ExtractFeatureInRedisResponse extractFeatureInRedisResponse = featureServiceBlockingStub.extractFeatureInRedis(facePicsInRedisRequest);
        return extractFeatureInRedisResponse.getSuccess().equals(DataConfig.SUCCESS);
    }

    @Override
    public CompareSimilarityDto compareFaceWithCollection(String faceKey) {

        ContrastPicWithCollesRequest build = ContrastPicWithCollesRequest.newBuilder()
                .setPicKey(faceKey)
                .addAllPicColles(IdentifyFacesContainer.getKeySet())
                .build();
        ContrastPicWithCollesResponse contrastPicWithCollesResponse = featureServiceBlockingStub.contrastPicWithCollesInRedis(build);

        List<Double> similarityOrderedListList = contrastPicWithCollesResponse.getSimilarityOrderedListList();
        ProtocolStringList picCollesList = contrastPicWithCollesResponse.getPicCollesList();

        if (CollectionUtils.isEmpty(similarityOrderedListList)){
//            没有结果
            return null;
        }

        CompareSimilarityDto compareSimilarityDto = new CompareSimilarityDto();
        if (similarityOrderedListList.get(similarityOrderedListList.size() -1) > DataConfig.MAX_SIMILARITY){
//            太似性
            compareSimilarityDto.setOverSimilarity(true);
            compareSimilarityDto.setSimilarity(similarityOrderedListList.get(0));
            compareSimilarityDto.setPicColleKey(picCollesList.get(0));
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

        compareSimilarityDto.setSimilarity(rightNum);
        compareSimilarityDto.setPicColleKey( picCollesList.get(similarityOrderedListList.indexOf(rightNum)) );
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
}
