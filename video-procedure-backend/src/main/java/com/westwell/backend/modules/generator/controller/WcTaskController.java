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

import com.westwell.backend.modules.generator.entity.WcTaskEntity;
import com.westwell.backend.modules.generator.service.WcTaskService;
import com.westwell.backend.common.utils.PageUtils;
import com.westwell.backend.common.utils.R;



/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
@RestController
@RequestMapping("generator/wctask")
public class WcTaskController {
    @Autowired
    private WcTaskService wcTaskService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("generator:wctask:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wcTaskService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{taskNo}")
    @RequiresPermissions("generator:wctask:info")
    public R info(@PathVariable("taskNo") Integer taskNo){
		WcTaskEntity wcTask = wcTaskService.getById(taskNo);

        return R.ok().put("wcTask", wcTask);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("generator:wctask:save")
    public R save(@RequestBody WcTaskEntity wcTask){
		wcTaskService.save(wcTask);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("generator:wctask:update")
    public R update(@RequestBody WcTaskEntity wcTask){
		wcTaskService.updateById(wcTask);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("generator:wctask:delete")
    public R delete(@RequestBody Integer[] taskNos){
		wcTaskService.removeByIds(Arrays.asList(taskNos));

        return R.ok();
    }

}
