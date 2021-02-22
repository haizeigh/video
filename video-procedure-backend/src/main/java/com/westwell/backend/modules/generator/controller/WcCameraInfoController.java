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

import com.westwell.backend.modules.generator.entity.WcCameraInfoEntity;
import com.westwell.backend.modules.generator.service.WcCameraInfoService;
import com.westwell.backend.common.utils.PageUtils;
import com.westwell.backend.common.utils.R;



/**
 * 摄像来源信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
@RestController
@RequestMapping("generator/wccamerainfo")
public class WcCameraInfoController {
    @Autowired
    private WcCameraInfoService wcCameraInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("generator:wccamerainfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wcCameraInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("generator:wccamerainfo:info")
    public R info(@PathVariable("id") Integer id){
		WcCameraInfoEntity wcCameraInfo = wcCameraInfoService.getById(id);

        return R.ok().put("wcCameraInfo", wcCameraInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("generator:wccamerainfo:save")
    public R save(@RequestBody WcCameraInfoEntity wcCameraInfo){
		wcCameraInfoService.save(wcCameraInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("generator:wccamerainfo:update")
    public R update(@RequestBody WcCameraInfoEntity wcCameraInfo){
		wcCameraInfoService.updateById(wcCameraInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("generator:wccamerainfo:delete")
    public R delete(@RequestBody Integer[] ids){
		wcCameraInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
