package com.westwell.backend.modules.generator.service;

public interface StudentPicService {

    void saveOnePicRedis(String key, String pic);

    String getOnePicFeature(String key);

    String saveOnePicRedisAndFeature(String key, String pic);

    void addStudentPicFeature(String featureKey);

    void updateStudentPicFeature(String featureKey);
}
