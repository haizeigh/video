package com.westwell.backend.modules.generator.controller;

import com.google.common.base.Strings;
import com.westwell.backend.common.utils.PageUtils;
import com.westwell.backend.common.utils.R;
import com.westwell.backend.modules.generator.entity.StudentBaseInfoEntity;
import com.westwell.backend.modules.generator.service.StudentBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 学生基本信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-08 10:15:59
 */
@Slf4j
@RestController
@RequestMapping("generator/studentbaseinfo")
public class StudentBaseInfoController {
    @Autowired
    private StudentBaseInfoService studentBaseInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("generator:studentbaseinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = studentBaseInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("generator:studentbaseinfo:info")
    public R info(@PathVariable("id") Integer id){
		StudentBaseInfoEntity studentBaseInfo = studentBaseInfoService.getById(id);

        return R.ok().put("studentBaseInfo", studentBaseInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("generator:studentbaseinfo:save")
    public R save(@RequestBody StudentBaseInfoEntity studentBaseInfo){
        try {
            studentBaseInfoService.saveInfo(studentBaseInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("新增图片错误");

        }
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("generator:studentbaseinfo:update")
    public R update(@RequestBody StudentBaseInfoEntity studentBaseInfo){
        try {
            studentBaseInfoService.updateInfoById(studentBaseInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("更新图片错误");
        }
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("generator:studentbaseinfo:delete")
    public R delete(@RequestBody Integer[] ids){
		studentBaseInfoService.removeInfoByIds(Arrays.asList(ids));
//        boolean result = studentBaseInfoService.deletePicByIds(ids);
//        if (!result){
//            return R.error("删除图片错误");
//        }
        return R.ok();
    }

    @RequestMapping("/save/path")
    public R saveByPath(@RequestParam String path){

        if (Strings.isNullOrEmpty(path)){
            return R.error("path is null");
        }
        try {
            studentBaseInfoService.saveByPath(path);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        }

        return R.ok();
    }

    @RequestMapping("/sync")
    public R updateList(String unit){

/*        if (Strings.isNullOrEmpty(path)){
            return R.error("path is null");
        }
        try {
            studentBaseInfoService.saveByPath(path);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        }*/
        studentBaseInfoService.syncUnit(unit);

        return R.ok();
    }

}
