package com.westwell.backend.modules.generator.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
@Data
@TableName("wc_task")
public class WcTaskEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 任务标识


	 */
	@TableId
	private Integer taskNo;
	/**
	 * 摄像头编号


	 */
	private Integer cameraNo;
	/**
	 * 视频的开始时间


	 */
	private Date videoStartTime;
	/**
	 * 视频的结束时间
	 */
	private Date videoEndTime;
	/**
	 * 每秒的帧数
	 */
	private Integer frame;
	/**
	 * 任务的开始时间
	 */
	private Date taskStartTime;
	/**
	 * 任务的结束时间
	 */
	private Date taskEndTime;
	/**
	 * 任务状态


	 */
	private String taskStatus;
	/**
	 * 任务备注
	 */
	private String context;

}
