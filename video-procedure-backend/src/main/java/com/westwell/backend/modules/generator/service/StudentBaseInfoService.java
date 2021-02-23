package com.westwell.backend.modules.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.westwell.backend.common.utils.PageUtils;
import com.westwell.backend.modules.generator.entity.StudentBaseInfoEntity;

import java.util.List;
import java.util.Map;

/**
 * 学生基本信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-08 10:15:59
 */
public interface StudentBaseInfoService extends IService<StudentBaseInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveByPath(String path) throws Exception;

    boolean saveStudentPic(StudentBaseInfoEntity studentBaseInfo) throws Exception;

    boolean deletePicByIds(List<Integer> ids);

    void saveInfo(StudentBaseInfoEntity studentBaseInfo) throws Exception;

    void updateInfoById(StudentBaseInfoEntity studentBaseInfo) throws Exception;

    void removeInfoByIds(List<Integer> ids);

    void syncUnit(String unit);
}

