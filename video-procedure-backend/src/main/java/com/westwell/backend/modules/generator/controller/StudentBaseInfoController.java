package com.westwell.backend.modules.generator.controller;

import java.util.Arrays;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.westwell.backend.modules.generator.entity.StudentBaseInfoEntity;
import com.westwell.backend.modules.generator.service.StudentBaseInfoService;
import com.westwell.backend.common.utils.PageUtils;
import com.westwell.backend.common.utils.R;


/**
 * 学生基本信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-08 10:15:59
 */
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
		studentBaseInfoService.save(studentBaseInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("generator:studentbaseinfo:update")
    public R update(@RequestBody StudentBaseInfoEntity studentBaseInfo){
		studentBaseInfoService.updateById(studentBaseInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("generator:studentbaseinfo:delete")
    public R delete(@RequestBody Integer[] ids){
		studentBaseInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
